package com.pickeat.backend.template.application.listener;

import com.pickeat.backend.global.cache.CacheKey;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TemplateCacheEventListener {

    private final CacheManager cacheManager;

    public TemplateCacheEventListener(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void processTemplateCacheInvalidationEvent(String message) {
        Optional<Long> templateId = parseTemplateCacheInvalidationMessage(message);
        if (templateId.isEmpty()) {
            return;
        }

        Cache templateListCache = cacheManager.getCache(CacheKey.TEMPLATE_LIST_CACHE_KEY.name());
        if (templateListCache != null) {
            templateListCache.clear();
        }

        Cache templateWishCache = cacheManager.getCache(CacheKey.TEMPLATE_WISH_CACHE_KEY.getValue());
        if (templateWishCache != null) {
            templateWishCache.evict(templateId.get());
        }
    }

    private Optional<Long> parseTemplateCacheInvalidationMessage(String message) {
        try {
            return Optional.of(Long.parseLong(message));
        } catch (NumberFormatException e) {
            log.error("적절하지 않는 템플릿 위시 캐시 무효화 이벤트를 전달받았습니다 : {}", message);
        }
        return Optional.empty();
    }
}
