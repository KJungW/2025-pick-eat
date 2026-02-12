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
import java.util.Set;
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
    class 식당_관련_데이터_세팅 {

        @Test
        void 식당_관련_데이터를_성공적으로_세팅한다() {
            // given
            RestaurantsV2 restaurants = new RestaurantsV2(List.of(
                    RestaurantV2Fixture.create("마라탕"),
                    RestaurantV2Fixture.create("돈가스")));
            String pickeatCode = "pickeat-code";
            String expectedKey = StorageKey.RESTAURANT_META.generateKey("pickeat-code");

            // when
            Boolean isSuccess = restaurantsStorage.setupRestaurants(pickeatCode, restaurants);

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
        void 픽잇에_이미_식당_관련_데이터가_세팅된_경우_예외를_발생시킨다() {
            // given
            RestaurantsV2 existing = new RestaurantsV2(List.of(RestaurantV2Fixture.create("기존 데이터")));
            String pickeatCode = "pickeat-code";
            restaurantsStorage.setupRestaurants(pickeatCode, existing);

            RestaurantsV2 newData = new RestaurantsV2(List.of(RestaurantV2Fixture.create("새 데이터")));

            // when
            Boolean result = restaurantsStorage.setupRestaurants(pickeatCode, newData);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    class 식당_메타정보_조회 {

        @Test
        void 식당의_메타정보를_성공적으로_조회한다() {
            // given
            RestaurantsV2 restaurants = new RestaurantsV2(List.of(RestaurantV2Fixture.create("삼겹살")));
            String pickeatCode = "pickeat-code";
            restaurantsStorage.setupRestaurants(pickeatCode, restaurants);

            // when
            Optional<RestaurantsV2> result = restaurantsStorage.getAllRestaurantMeta(pickeatCode);

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
            Optional<RestaurantsV2> result = restaurantsStorage.getAllRestaurantMeta(nonExistentCode);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    class 식당_소거 {

        @Test
        void 특정_식당들을_소거_처리할_수_있다() {
            // given
            String pickeatCode = "pickeat-code";
            RestaurantsV2 restaurants = new RestaurantsV2(List.of(
                    RestaurantV2Fixture.create("마라탕"),
                    RestaurantV2Fixture.create("돈가스"),
                    RestaurantV2Fixture.create("쌀국수")));
            restaurantsStorage.setupRestaurants(pickeatCode, restaurants);

            List<String> restaurantsCodes = restaurants.extrudeRestaurantCodes();

            // when
            restaurantsStorage.excludeRestaurants(pickeatCode,
                    List.of(restaurantsCodes.get(0), restaurantsCodes.get(1)));

            // then
            Set<String> remainCodes = restaurantsStorage.getAliveRestaurantCode(pickeatCode);
            assertAll(
                    () -> assertThat(remainCodes).hasSize(1),
                    () -> assertThat(remainCodes).containsExactlyInAnyOrder(restaurantsCodes.get(2))
            );
        }
    }

    @Nested
    class 소거되지_않은_식당_조회 {

        @Test
        void 소거되지_않은_식당들을_조회할_수_있다() {
            // given
            String pickeatCode = "pickeat-code";
            RestaurantsV2 restaurants = new RestaurantsV2(List.of(
                    RestaurantV2Fixture.create("마라탕"),
                    RestaurantV2Fixture.create("돈가스")));
            restaurantsStorage.setupRestaurants(pickeatCode, restaurants);

            List<String> restaurantsCodes = restaurants.extrudeRestaurantCodes();
            restaurantsStorage.excludeRestaurants(pickeatCode, List.of(restaurantsCodes.get(0)));

            // when
            Set<String> aliveCodes = restaurantsStorage.getAliveRestaurantCode(pickeatCode);

            // then
            assertAll(
                    () -> assertThat(aliveCodes).hasSize(1),
                    () -> assertThat(aliveCodes).containsExactlyInAnyOrder(restaurantsCodes.get(1))
            );
        }
    }
}
