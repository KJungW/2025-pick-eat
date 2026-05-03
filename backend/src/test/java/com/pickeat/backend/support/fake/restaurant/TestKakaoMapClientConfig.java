package com.pickeat.backend.support.fake.restaurant;

import com.pickeat.backend.restaurant.application.RestaurantSearchClient;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("test")
@Configuration
public class TestKakaoMapClientConfig {

    @Bean
    public RestaurantSearchClient restaurantSearchClient() {
        return new FakeRestaurantSearchClient();
    }

    @Bean
    public Bucket kakaoRestaurantSearchBucket(ProxyManager<String> proxyManager) {
        BucketConfiguration configuration = BucketConfiguration.builder()
                .addLimit(limit -> limit.capacity(1_000).refillGreedy(1_000, Duration.ofSeconds(1)))
                .build();
        return proxyManager.builder().build("test-kakao-api-limit", configuration);
    }
}

