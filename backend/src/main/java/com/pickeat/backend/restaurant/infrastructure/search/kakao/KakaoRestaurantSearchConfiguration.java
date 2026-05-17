package com.pickeat.backend.restaurant.infrastructure.search.kakao;

import static com.pickeat.backend.global.configuration.profile.EnvironmentProfile.DEVELOPMENT;
import static com.pickeat.backend.global.configuration.profile.EnvironmentProfile.LOCAL;
import static com.pickeat.backend.global.configuration.profile.EnvironmentProfile.PRODUCTION;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pickeat.backend.restaurant.application.client.RestaurantSearchClient;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Profile({LOCAL, DEVELOPMENT, PRODUCTION})
@Configuration
@RequiredArgsConstructor
public class KakaoRestaurantSearchConfiguration {

    private static final String BUCKET_NAME = "kakao-map-api-limit";
    private static final int BUCKET_TOKEN_CAPACITY = 20;

    private final ObjectMapper objectMapper;

    @Bean
    public RestaurantSearchClient kakaoRestaurantSearchClient(
            @Value("${external.kakao.map.baseUrl}") String baseUrl,
            @Value("${external.kakao.map.restApiKey}") String apikey,
            @Value("${external.kakao.map.baseUrl}") Integer readTimeOut,
            @Value("${external.kakao.map.baseUrl}") Integer connectTimeout,
            KakaoRestaurantSearchParser kakaoRestaurantSearchParser,
            Bucket kakaoRestaurantSearchBucket
    ) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeOut);

        RestClient restClient = RestClient.builder()
                .requestFactory(factory)
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "KakaoAK " + apikey)
                .defaultHeader("Content-Type", "application/json")
                .build();

        return new KakaoRestaurantSearchClient(
                restClient,
                objectMapper,
                kakaoRestaurantSearchParser,
                kakaoRestaurantSearchBucket
        );
    }

    @Bean
    public KakaoRestaurantSearchParser kakaoRestaurantSearchParser() {
        return new KakaoRestaurantSearchParser();
    }

    @Bean
    public Bucket kakaoRestaurantSearchBucket(ProxyManager<String> proxyManager) {
        BucketConfiguration config = BucketConfiguration.builder()
                .addLimit(limit -> limit
                        .capacity(BUCKET_TOKEN_CAPACITY)
                        .refillGreedy(BUCKET_TOKEN_CAPACITY, Duration.ofSeconds(1)))
                .build();
        return proxyManager.builder().build(BUCKET_NAME, () -> config);
    }
}
