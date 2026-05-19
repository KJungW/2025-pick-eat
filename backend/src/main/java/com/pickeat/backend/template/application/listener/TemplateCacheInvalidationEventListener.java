package com.pickeat.backend.template.application.listener;

import static com.pickeat.backend.global.exception.code.ServerErrorCode.INTERNAL_SERVER_ERROR;

import com.pickeat.backend.global.configuration.cache.CacheKey;
import com.pickeat.backend.global.exception.type.ServerException;
import com.pickeat.backend.global.log.LogWriter;
import com.pickeat.backend.global.log.model.error.ServerErrorLog;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TemplateCacheInvalidationEventListener implements MessageListener {

    private static final long JITTER_DELAY_MS = 500L;

    private final CacheManager cacheManager;
    private final TaskExecutor virtualThreadExecutor;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        Long templateId = parseMessage(message);
        if (templateId == null) {
            return;
        }

        virtualThreadExecutor.execute(() -> {
            try {
                waitJitterDelay();
                invalidateAllTemplateCache();
                invalidateTemplateWishCache(templateId);
            } catch (InterruptedException e) { // Jitter 대기에서 깨어날 때 인터럽트 발생
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                ServerErrorLog errorLog = ServerErrorLog.of(new ServerException(INTERNAL_SERVER_ERROR, e));
                LogWriter.error(this.getClass(), errorLog);
            }
        });
    }

    private void waitJitterDelay() throws InterruptedException {
        long jitterDelay = ThreadLocalRandom.current().nextLong(JITTER_DELAY_MS);
        Thread.sleep(Duration.ofMillis(jitterDelay));
    }

    private void invalidateAllTemplateCache() {
        Cache templateListCache = cacheManager.getCache(CacheKey.TEMPLATE_LIST_CACHE_KEY.getValue());
        if (templateListCache != null) {
            templateListCache.clear();
        }
    }

    private void invalidateTemplateWishCache(Long templateId) {
        Cache templateWishCache = cacheManager.getCache(CacheKey.TEMPLATE_WISH_CACHE_KEY.getValue());
        if (templateWishCache != null) {
            templateWishCache.evict(templateId);
        }
    }

    private Long parseMessage(Message message) {
        try {
            String messageBody = new String(message.getBody(), StandardCharsets.UTF_8);
            return Long.parseLong(messageBody);
        } catch (NumberFormatException e) {
            ServerErrorLog errorLog = ServerErrorLog.of(new ServerException(INTERNAL_SERVER_ERROR, e));
            LogWriter.error(this.getClass(), errorLog);
        }
        return null;
    }
}
