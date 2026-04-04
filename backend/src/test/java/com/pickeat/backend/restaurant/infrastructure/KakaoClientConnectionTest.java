package com.pickeat.backend.restaurant.infrastructure;

import static org.assertj.core.api.Assertions.assertThatCode;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pickeat.backend.restaurant.application.dto.request.RestaurantSearchRequest;
import com.pickeat.backend.support.AcceptanceTest;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Disabled("외부 네트워크 요청이 일어나므로 비활성화")
class KakaoClientConnectionTest extends AcceptanceTest {

    @Value("${external.kakao.map.restApiKey}")
    private String kakaoApiKey;

    private KakaoRestaurantSearchClient client;

    @BeforeEach
    void setup() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(10000);
        RestClient restClient = RestClient.builder()
                .requestFactory(factory)
                .baseUrl("https://dapi.kakao.com")
                .defaultHeader("Authorization", "KakaoAK " + kakaoApiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();

        Bucket testBucket = Bucket
                .builder()
                .addLimit(Bandwidth.builder()
                        .capacity(1_000_000)
                        .refillGreedy(1_000_000, Duration.ofSeconds(1))
                        .build())
                .build();

        this.client = new KakaoRestaurantSearchClient(restClient, new ObjectMapper(), testBucket);
    }

    @Test
    void 실제_카카오_API_연동_테스트() {
        // when & then
        assertThatCode(() -> client.getRestaurants(
                new RestaurantSearchRequest("패스트푸드", 127.103068896795, 37.5152535228382, 200, 10)))
                .doesNotThrowAnyException();
    }
}
