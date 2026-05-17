package com.pickeat.backend.restaurant.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pickeat.backend.global.exception.type.ExternalException;
import com.pickeat.backend.restaurant.application.client.RestaurantSearchClient;
import com.pickeat.backend.restaurant.application.dto.RestaurantInfoDto;
import com.pickeat.backend.restaurant.application.dto.request.LocationRestaurantRequest;
import com.pickeat.backend.support.DatabaseSliceTest;
import com.pickeat.backend.support.fake.restaurant.TestKakaoMapClientConfig;
import com.pickeat.backend.support.fixture.RestaurantRequestFixture;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.core.task.VirtualThreadTaskExecutor;
import org.springframework.http.HttpStatus;

@Import({LocationRestaurantSearchService.class, TestKakaoMapClientConfig.class})
class LocationRestaurantSearchServiceTest extends DatabaseSliceTest {

    @Nested
    class 위치_기반_식당_조회 {

        @Autowired
        private RestaurantSearchClient restaurantSearchClient;

        @Autowired
        private VirtualThreadTaskExecutor virtualThreadTaskExecutor;

        private LocationRestaurantSearchService locationRestaurantSearchService;

        @BeforeEach
        void setup() {
            this.locationRestaurantSearchService = new LocationRestaurantSearchService(
                    restaurantSearchClient,
                    virtualThreadTaskExecutor
            );
        }


        @Test
        void 식당을_성공적으로_조회한다() {
            // given
            LocationRestaurantRequest request = new LocationRestaurantRequest(127.0, 37.0, 1000);

            // when
            List<RestaurantInfoDto> result = locationRestaurantSearchService.searchByLocation(request);

            // then
            assertAll(
                    () -> assertThat(result).isNotEmpty(),
                    () -> assertThat(result).doesNotHaveDuplicates()
            );
        }

        @Test
        void 외부_API_호출을_가상_스레드로_병렬처리_한다() {
            // given
            RestaurantSearchClient restaurantSearchClient = mock(RestaurantSearchClient.class);
            this.locationRestaurantSearchService =
                    new LocationRestaurantSearchService(restaurantSearchClient, virtualThreadTaskExecutor);

            when(restaurantSearchClient.getRestaurants(any()))
                    .thenAnswer(invocation -> {
                        assertThat(Thread.currentThread().isVirtual()).isTrue(); // 가상 스레드인지 확인
                        return List.of(RestaurantRequestFixture.create(Thread.currentThread().getName() + "식당"));
                    });

            LocationRestaurantRequest request = new LocationRestaurantRequest(127.0, 37.0, 1000);

            // when
            List<RestaurantInfoDto> result = locationRestaurantSearchService.searchByLocation(request);

            // then
            assertAll(
                    () -> assertThat(result).hasSize(5),
                    () -> verify(restaurantSearchClient, times(5)).getRestaurants(any())
            );
        }

        @Test
        void 일부_외부_API_호출_실패시_전체_실패처리_한다() {
            // given
            RestaurantSearchClient restaurantSearchClient = mock(RestaurantSearchClient.class);
            this.locationRestaurantSearchService =
                    new LocationRestaurantSearchService(restaurantSearchClient, virtualThreadTaskExecutor);

            when(restaurantSearchClient.getRestaurants(any()))
                    .thenReturn(List.of(RestaurantRequestFixture.create("정상 식당"))) // 1번째 호출 성공
                    .thenThrow(new ExternalException("카카오 서버 장애", "KAKAO", HttpStatus.valueOf(500))) // 2번째 호출 실패
                    .thenReturn(List.of(RestaurantRequestFixture.create("데이터")));  // 이후 호출들...

            LocationRestaurantRequest request = new LocationRestaurantRequest(127.0, 37.0, 1000);

            // when & then
            assertThatThrownBy(() -> locationRestaurantSearchService.searchByLocation(request))
                    .isInstanceOf(ExternalException.class)
                    .hasMessage("카카오 서버 장애");
        }
    }
}
