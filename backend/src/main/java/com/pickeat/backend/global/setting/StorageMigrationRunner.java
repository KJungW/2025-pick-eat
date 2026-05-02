package com.pickeat.backend.global.setting;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class StorageMigrationRunner implements CommandLineRunner {

    private static final String SCRIPT_DIRECTORY_ADDRESS = "classpath:storage/migration/*.lua";

    private final StorageMigrationHistoryService historyService;
    private final StringRedisTemplate redisTemplate;
    private final ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();

    @Override
    public void run(String... args) throws Exception {
        log.info("Storage 키 배치 마이그레이션을 시작합니다...");

        List<Resource> pendingScripts = findPendingMigrationScript();
        if (pendingScripts.isEmpty()) {
            log.info("실행할 새로운 마이그레이션 스크립트가 없습니다.");
            return;
        }

        List<StorageMigrationHistory> histories;
        try {
            histories = historyService.reserveAllHistory(pendingScripts);
        } catch (Exception e) {
            log.warn("현재 다른 서버에서 마이그레이션이 진행 중입니다. 작업을 중단하고 애플리케이션을 기동합니다.");
            return;
        }

        for (int i = 0; i < pendingScripts.size(); i++) {
            processMigration(pendingScripts.get(i), histories.get(i));
        }
    }

    private List<Resource> findPendingMigrationScript() throws IOException {
        Resource[] resources = resourceResolver.getResources(SCRIPT_DIRECTORY_ADDRESS);
        return Arrays.stream(resources)
                .sorted(Comparator.comparing(Resource::getFilename))
                .filter(script -> !checkScriptComplete(script))
                .toList();
    }

    private void processMigration(Resource script, StorageMigrationHistory history) {
        String fileName = script.getFilename();
        Long version = extrudeScriptVersion(script.getFilename());

        try {
            log.info("마이그레이션 실행: {} (V{})", fileName, version);

            String scriptSource = StreamUtils.copyToString(script.getInputStream(), StandardCharsets.UTF_8);
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            redisScript.setScriptText(scriptSource);
            redisScript.setResultType(Long.class);

            Long resultCount = redisTemplate.execute(redisScript, Collections.emptyList());

            historyService.completeMigration(history.getId());
            log.info("마이그레이션 완료: V{} (처리된 키: {}개)", version, resultCount);

        } catch (Exception e) {
            historyService.failMigration(history.getId());
            log.error("마이그레이션 실패: V{}", version, e);
            throw new RuntimeException("실패로 인해 마이그레이션을 중단합니다.", e);
        }
    }

    private boolean checkScriptComplete(Resource script) {
        Long version = extrudeScriptVersion(script.getFilename());
        return historyService.isCompleteHistory(version);
    }

    private Long extrudeScriptVersion(String scriptFileName) {
        return Long.parseLong(scriptFileName.split("__")[0].substring(1));
    }
}
