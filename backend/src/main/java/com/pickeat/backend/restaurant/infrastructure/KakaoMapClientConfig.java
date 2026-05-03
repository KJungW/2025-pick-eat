package com.pickeat.backend.restaurant.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pickeat.backend.restaurant.application.RestaurantSearchClient;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Profile({"local", "dev", "prod"})
@Configuration
@RequiredArgsConstructor
public class KakaoMapClientConfig {

    private final ObjectMapper objectMapper;

    @Bean
    public RestaurantSearchClient kakaoRestaurantSearchClient(
            KakaoMapApiProperties properties,
            Bucket kakaoRestaurantSearchBucket
    ) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.getConnectTimeout());
        factory.setReadTimeout(properties.getReadTimeout());

        RestClient restClient = RestClient.builder()
                .requestFactory(factory)
                .baseUrl(properties.getBaseUrl())
                .defaultHeader("Authorization", "KakaoAK " + properties.getRestApiKey())
                .defaultHeader("Content-Type", "application/json")
                .build();

        return new KakaoRestaurantSearchClient(restClient, objectMapper, kakaoRestaurantSearchBucket);
    }

    @Bean
    public Bucket kakaoRestaurantSearchBucket(ProxyManager<String> proxyManager) {
        BucketConfiguration config = BucketConfiguration.builder()
                .addLimit(limit -> limit.capacity(20).refillGreedy(20, Duration.ofSeconds(1)))
                .build();
        return proxyManager.builder().build("kakao-map-api-limit", () -> config);
    }
}
