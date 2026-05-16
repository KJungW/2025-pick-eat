package com.pickeat.backend.template.application.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.pickeat.backend.global.configuration.cache.CacheKey;
import com.pickeat.backend.support.DatabaseSliceTest;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.DefaultMessage;

@Import(value = {TemplateCacheEventListener.class})
class TemplateCacheEventListenerTest extends DatabaseSliceTest {

    @Autowired
    private TemplateCacheEventListener eventListener;

    @Autowired
    private CacheManager cacheManager;

    @Test
    void 캐시_무효화_이벤트_발생시_캐시_무효화() {
        // given
        Cache listCache = cacheManager.getCache(CacheKey.Holder.TEMPLATE_LIST_CACHE_KEY);
        listCache.put("0_10", "list-data-1");
        listCache.put("10_10", "list-data-2");

        Cache wishCache = cacheManager.getCache(CacheKey.Holder.TEMPLATE_WISH_CACHE_KEY);
        wishCache.put(1L, "wish-data-1");
        wishCache.put(2L, "wish-data-2");

        // when
        eventListener.onMessage(new DefaultMessage("template-topic".getBytes(), "1".getBytes()), null);

        // then (최대 1초 동안 기다리며 캐시가 제대로 무효화되는지 확인)
        org.awaitility.Awaitility.await()
                .atMost(1, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    assertThat(listCache.get("0_10")).isNull();
                    assertThat(listCache.get("10_10")).isNull();
                    assertThat(wishCache.get(1L)).isNull();
                    assertThat(wishCache.get(2L).get()).isEqualTo("wish-data-2");
                });
    }

    @Test
    void 잘못된_메시지_발생시_무시_처리() {
        // given
        Cache listCache = cacheManager.getCache(CacheKey.Holder.TEMPLATE_LIST_CACHE_KEY);
        listCache.put("0_10", "list-data-1");
        listCache.put("10_10", "list-data-2");

        Cache wishCache = cacheManager.getCache(CacheKey.Holder.TEMPLATE_WISH_CACHE_KEY);
        wishCache.put(1L, "wish-data-1");
        wishCache.put(2L, "wish-data-2");

        // when
        eventListener.onMessage(new DefaultMessage("template-topic".getBytes(), "invalid_id".getBytes()), null);

        // then (최대 1초 동안 기다리며 캐시가 무효화되지 않는 것을 확인)
        org.awaitility.Awaitility.await()
                .during(1, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    assertAll(
                            () -> assertThat(listCache.get("0_10").get()).isEqualTo("list-data-1"),
                            () -> assertThat(listCache.get("10_10").get()).isEqualTo("list-data-2"),
                            () -> assertThat(wishCache.get(1L).get()).isEqualTo("wish-data-1"),
                            () -> assertThat(wishCache.get(2L).get()).isEqualTo("wish-data-2")
                    );
                });
    }
}
