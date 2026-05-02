package com.pickeat.backend.global.setting;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StorageMigrationHistoryService {

    private final StorageMigrationHistoryRepository historyRepository;

    @Transactional
    public List<StorageMigrationHistory> reserveAllHistory(List<Resource> scripts) {
        try {
            List<StorageMigrationHistory> histories = scripts.stream()
                    .map(script -> {
                        String fileName = script.getFilename();
                        return new StorageMigrationHistory(extrudeScriptVersion(script.getFilename()), fileName);
                    })
                    .toList();
            return historyRepository.saveAllAndFlush(histories);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("마이그레이션 대상 선점 중 충돌 발생", e);
        }
    }

    @Transactional
    public void completeMigration(Long historyId) {
        StorageMigrationHistory history = getById(historyId);
        history.completeMigration();
    }

    @Transactional
    public void failMigration(Long historyId) {
        StorageMigrationHistory history = getById(historyId);
        history.failMigration();
    }

    public boolean isCompleteHistory(Long version) {
        return historyRepository.existsByVersionAndState(version, StorageMigrationState.COMPLETE);
    }

    private StorageMigrationHistory getById(Long historyId) {
        return historyRepository.findById(historyId).orElseThrow(
                () -> new RuntimeException("해당 ID의 마이그레이션 히스토리가 존재하지 않습니다."));
    }

    private Long extrudeScriptVersion(String scriptFileName) {
        return Long.parseLong(scriptFileName.split("__")[0].substring(1));
    }
}
