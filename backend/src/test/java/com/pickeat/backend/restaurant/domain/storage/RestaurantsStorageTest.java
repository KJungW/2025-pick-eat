package com.pickeat.backend.restaurant.domain.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.pickeat.backend.global.setting.StorageKey;
import com.pickeat.backend.global.utility.JsonParser;
import com.pickeat.backend.restaurant.domain.RestaurantsV2;
import com.pickeat.backend.support.DatabaseSliceTest;
import com.pickeat.backend.support.fixture.RestaurantV2Fixture;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;

@Import(RestaurantsStorage.class)
class RestaurantsStorageTest extends DatabaseSliceTest {

    @Autowired
    private RestaurantsStorage restaurantsStorage;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private JsonParser jsonParser;

    @Nested
    class 식당_저장 {

        @Test
        void 식당을_성공적으로_저장한다() {
            // given
            RestaurantsV2 restaurants = new RestaurantsV2(List.of(
                    RestaurantV2Fixture.create("마라탕"),
                    RestaurantV2Fixture.create("돈가스")));
            String pickeatCode = "pickeat-code";
            String expectedKey = StorageKey.RESTAURANT.generateKey("pickeat-code");

            // when
            Boolean isSuccess = restaurantsStorage.saveIfAbsent(restaurants, pickeatCode);

            // then
            String jsonValue = redisTemplate.opsForValue().get(expectedKey);
            RestaurantsV2 saved = jsonParser.fromJson(jsonValue, RestaurantsV2.class);
            assertAll(
                    () -> assertThat(isSuccess).isTrue(),
                    () -> assertThat(saved).isNotNull(),
                    () -> assertThat(saved.getRestaurants()).hasSize(2),
                    () -> assertThat(saved.getRestaurants().get(0).getName()).isEqualTo("마라탕")
            );
        }

        @Test
        void 픽잇에_이미_식당이_존재하는_경우_식당을_생성할_수_없다() {
            // given
            RestaurantsV2 existing = new RestaurantsV2(List.of(RestaurantV2Fixture.create("기존 데이터")));
            String pickeatCode = "pickeat-code";
            restaurantsStorage.saveIfAbsent(existing, pickeatCode);

            RestaurantsV2 newData = new RestaurantsV2(List.of(RestaurantV2Fixture.create("새 데이터")));

            // when
            Boolean result = restaurantsStorage.saveIfAbsent(newData, pickeatCode);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    class 식당_조회 {

        @Test
        void 식당을_성공적으로_조회한다() {
            // given
            RestaurantsV2 restaurants = new RestaurantsV2(List.of(RestaurantV2Fixture.create("삼겹살")));
            String pickeatCode = "pickeat-code";
            restaurantsStorage.saveIfAbsent(restaurants, pickeatCode);

            // when
            Optional<RestaurantsV2> result = restaurantsStorage.get(pickeatCode);

            // then
            assertAll(
                    () -> assertThat(result).isPresent(),
                    () -> assertThat(result.get().getRestaurants().get(0).getName()).isEqualTo("삼겹살")
            );
        }

        @Test
        void 조회할_식당이_없는_경우_빈_리스트를_조회한다() {
            // given
            String nonExistentCode = "EMPTY_CODE";

            // when
            Optional<RestaurantsV2> result = restaurantsStorage.get(nonExistentCode);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    class 식당_제거 {

        @Test
        void 식당을_성공적으로_제거한다() {
            // given
            RestaurantsV2 restaurants = new RestaurantsV2(List.of(RestaurantV2Fixture.create("마라탕")));
            String pickeatCode = "pickeat-code";
            restaurantsStorage.saveIfAbsent(restaurants, pickeatCode);

            String expectedKey = StorageKey.RESTAURANT.generateKey(pickeatCode);

            // when
            restaurantsStorage.remove(pickeatCode);

            // then
            String jsonValue = redisTemplate.opsForValue().get(expectedKey);
            Optional<RestaurantsV2> result = restaurantsStorage.get(pickeatCode);

            assertAll(
                    () -> assertThat(jsonValue).isNull(),
                    () -> assertThat(result).isEmpty()
            );
        }
    }
}
