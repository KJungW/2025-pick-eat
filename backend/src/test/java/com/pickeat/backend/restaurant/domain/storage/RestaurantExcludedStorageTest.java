package com.pickeat.backend.restaurant.domain.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.pickeat.backend.support.DatabaseSliceTest;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;

@Import(RestaurantExcludedStorage.class)
class RestaurantExcludedStorageTest extends DatabaseSliceTest {

    @Autowired
    private RestaurantExcludedStorage excludedStorage;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Nested
    class 식당_소거_세팅 {

        @Test
        void 식당_소거를_성공적으로_세팅한다() {
            // given
            String pickeatCode = "pickeat-code";
            List<String> restaurantCodes = List.of("RES001", "RES002", "RES003");

            // when
            boolean isSuccess = excludedStorage.setupExclude(pickeatCode, restaurantCodes);

            // then
            Set<String> savedCodes = excludedStorage.findAlive(pickeatCode);
            assertAll(
                    () -> assertThat(isSuccess).isTrue(),
                    () -> assertThat(savedCodes).hasSize(3),
                    () -> assertThat(savedCodes).containsAll(restaurantCodes)
            );
        }

        @Test
        void 이미_식당_소거가_세팅되어_있는_경우_예외를_발생시킨다() {
            // given
            String pickeatCode = "pickeat-code";
            List<String> firstCodes = List.of("RES001");
            excludedStorage.setupExclude(pickeatCode, firstCodes);

            List<String> secondCodes = List.of("RES002");

            // when
            boolean isSuccess = excludedStorage.setupExclude(pickeatCode, secondCodes);

            // then
            assertAll(
                    () -> assertThat(isSuccess).isFalse(),
                    () -> assertThat(excludedStorage.findAlive(pickeatCode)).containsExactly("RES001")
            );
        }
    }

    @Nested
    class 식당_소거 {

        @Test
        void 특정_식당들을_소거_처리할_수_있다() {
            // given
            String pickeatCode = "pickeat-code";
            List<String> restaurantCodes = List.of("RES001", "RES002", "RES003");
            excludedStorage.setupExclude(pickeatCode, restaurantCodes);

            // when
            excludedStorage.exclude(pickeatCode, List.of("RES001", "RES002"));

            // then
            Set<String> remainCodes = excludedStorage.findAlive(pickeatCode);
            assertAll(
                    () -> assertThat(remainCodes).hasSize(1),
                    () -> assertThat(remainCodes).containsExactly("RES003")
            );
        }
    }

    @Nested
    class 소거되지_않은_식당_조회 {

        @Test
        void 소거되지_않은_식당들을_조회할_수_있다() {
            // given
            String pickeatCode = "pickeat-code";
            List<String> restaurantCodes = List.of("RES001", "RES002");
            excludedStorage.setupExclude(pickeatCode, restaurantCodes);

            // when
            Set<String> aliveCodes = excludedStorage.findAlive(pickeatCode);

            // then
            assertThat(aliveCodes).containsExactlyInAnyOrder("RES001", "RES002");
        }
    }
}
