package com.pickeat.backend.support.utility;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
@RequiredArgsConstructor
public class CacheCleaner {

    private final CacheManager cacheManager;

    public void execute() {
        cacheManager.getCacheNames()
                .forEach(cacheName -> cacheManager.getCache(cacheName).clear());
    }
}
