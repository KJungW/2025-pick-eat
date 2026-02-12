package com.pickeat.backend.restaurant.domain.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.pickeat.backend.global.utility.JsonParser;
import com.pickeat.backend.restaurant.application.dto.RestaurantStateDto;
import com.pickeat.backend.restaurant.domain.RestaurantV2;
import com.pickeat.backend.restaurant.domain.RestaurantsV2;
import com.pickeat.backend.support.DatabaseSliceTest;
import com.pickeat.backend.support.fixture.RestaurantV2Fixture;
import java.util.List;
import java.util.Map;
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
        void 식당_메타데이터를_성공적으로_세팅한다() {
            // given
            RestaurantsV2 restaurants = new RestaurantsV2(List.of(
                    RestaurantV2Fixture.create("마라탕"),
                    RestaurantV2Fixture.create("돈가스")));
            String pickeatCode = "pickeat-code";

            // when
            Boolean isSuccess = restaurantsStorage.setupRestaurants(pickeatCode, restaurants);

            // then
            RestaurantsV2 savedMeta = restaurantsStorage.getAllRestaurantMeta(pickeatCode).get();
            assertAll(
                    () -> assertThat(isSuccess).isTrue(),
                    () -> assertThat(savedMeta.getRestaurants()).hasSize(2),
                    () -> assertThat(savedMeta.getRestaurants())
                            .extracting(RestaurantV2::getName)
                            .containsExactlyInAnyOrder("마라탕", "돈가스")
            );
        }

        @Test
        void 생존_식당_코드목록을_성공적으로_세팅한다() {
            // given
            RestaurantsV2 restaurants = new RestaurantsV2(List.of(
                    RestaurantV2Fixture.create("마라탕"),
                    RestaurantV2Fixture.create("돈가스")));
            String pickeatCode = "pickeat-code";

            // when
            Boolean isSuccess = restaurantsStorage.setupRestaurants(pickeatCode, restaurants);

            // then
            List<String> restaurantCodes = restaurants.extrudeRestaurantCodes();
            Set<String> aliveCodes = restaurantsStorage
                    .getAllRestaurantState(pickeatCode).aliveRestaurantCode();
            assertAll(
                    () -> assertThat(isSuccess).isTrue(),
                    () -> assertThat(aliveCodes).hasSize(2),
                    () -> assertThat(aliveCodes).containsExactlyInAnyOrderElementsOf(restaurantCodes)
            );
        }

        @Test
        void 식당_좋아요_합계를_성공적으로_세팅한다() {
            // given
            RestaurantsV2 restaurants = new RestaurantsV2(List.of(
                    RestaurantV2Fixture.create("마라탕"),
                    RestaurantV2Fixture.create("돈가스")));
            String pickeatCode = "pickeat-code";

            // when
            Boolean isSuccess = restaurantsStorage.setupRestaurants(pickeatCode, restaurants);

            // then
            List<String> restaurantCodes = restaurants.extrudeRestaurantCodes();
            Map<String, Integer> savedLikeCount = restaurantsStorage
                    .getAllRestaurantState(pickeatCode).likeCountByRestaurant();
            assertAll(
                    () -> assertThat(isSuccess).isTrue(),
                    () -> assertThat(savedLikeCount).hasSize(2),
                    () -> assertThat(savedLikeCount.get(restaurantCodes.get(0))).isEqualTo(0),
                    () -> assertThat(savedLikeCount.get(restaurantCodes.get(1))).isEqualTo(0)
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
    class 식당_상태정보_조회 {

        @Test
        void 식당의_상태정보를_성공적으로_조회한다() {
            // given
            String pickeatCode = "pickeat-code";
            RestaurantV2 restaurant1 = RestaurantV2Fixture.create("마라탕");
            RestaurantV2 restaurant2 = RestaurantV2Fixture.create("돈가스");
            RestaurantsV2 restaurants = new RestaurantsV2(List.of(restaurant1, restaurant2));

            restaurantsStorage.setupRestaurants(pickeatCode, restaurants);

            restaurantsStorage.like(pickeatCode, "user-1", restaurant1.getCode());
            restaurantsStorage.like(pickeatCode, "user-2", restaurant1.getCode());
            restaurantsStorage.excludeRestaurants(pickeatCode, List.of(restaurant2.getCode()));

            // when
            RestaurantStateDto result = restaurantsStorage.getAllRestaurantState(pickeatCode);

            // then
            assertAll(
                    () -> assertThat(result.aliveRestaurantCode()).hasSize(1),
                    () -> assertThat(result.aliveRestaurantCode()).containsExactly(restaurant1.getCode()),
                    () -> assertThat(result.likeCountByRestaurant()).hasSize(2),
                    () -> assertThat(result.likeCountByRestaurant().get(restaurant1.getCode())).isEqualTo(2),
                    () -> assertThat(result.likeCountByRestaurant().get(restaurant2.getCode())).isEqualTo(0)
            );
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
            Set<String> remainCodes = restaurantsStorage
                    .getAllRestaurantState(pickeatCode).aliveRestaurantCode();
            assertAll(
                    () -> assertThat(remainCodes).hasSize(1),
                    () -> assertThat(remainCodes).containsExactlyInAnyOrder(restaurantsCodes.get(2))
            );
        }
    }

    @Nested
    class 식당_좋아요 {

        @Test
        void 식당에_좋아요를_성공적으로_추가할_수_있다() {
            // given
            String pickeatCode = "pickeat-code";
            String participantCode = "user-1";
            RestaurantV2 restaurant = RestaurantV2Fixture.create("마라탕");
            RestaurantsV2 restaurants = new RestaurantsV2(List.of(restaurant));
            restaurantsStorage.setupRestaurants(pickeatCode, restaurants);

            // when
            boolean firstLike = restaurantsStorage.like(pickeatCode, participantCode, restaurant.getCode());

            // then
            Map<String, Integer> likeCounts = restaurantsStorage
                    .getAllRestaurantState(pickeatCode).likeCountByRestaurant();
            assertAll(
                    () -> assertThat(firstLike).isTrue(),
                    () -> assertThat(likeCounts.get(restaurant.getCode())).isEqualTo(1)
            );
        }

        @Test
        void 이미_좋아요를_추가한_식당에_좋아요를_추가할_수_없다() {
            // given
            String pickeatCode = "pickeat-code";
            String participantCode = "user-1";
            RestaurantV2 restaurant = RestaurantV2Fixture.create("마라탕");
            RestaurantsV2 restaurants = new RestaurantsV2(List.of(restaurant));
            restaurantsStorage.setupRestaurants(pickeatCode, restaurants);
            restaurantsStorage.like(pickeatCode, participantCode, restaurant.getCode());

            // when
            boolean secondLike = restaurantsStorage.like(pickeatCode, participantCode, restaurant.getCode());

            // then
            Map<String, Integer> likeCounts = restaurantsStorage
                    .getAllRestaurantState(pickeatCode).likeCountByRestaurant();
            assertAll(
                    () -> assertThat(secondLike).isFalse(),
                    () -> assertThat(likeCounts.get(restaurant.getCode())).isEqualTo(1)
            );
        }
    }

    @Nested
    class 식당_좋아요_취소 {

        @Test
        void 식당에_좋아요를_성공적으로_취소할_수_있다() {
            // given
            String pickeatCode = "pickeat-code";
            String participantCode = "user-1";
            RestaurantV2 restaurant = RestaurantV2Fixture.create("마라탕");
            RestaurantsV2 restaurants = new RestaurantsV2(List.of(restaurant));
            restaurantsStorage.setupRestaurants(pickeatCode, restaurants);
            restaurantsStorage.like(pickeatCode, participantCode, restaurant.getCode());

            // when
            boolean isCancelled = restaurantsStorage.cancelLike(pickeatCode, participantCode, restaurant.getCode());

            // then
            Map<String, Integer> likeCounts = restaurantsStorage
                    .getAllRestaurantState(pickeatCode).likeCountByRestaurant();
            assertAll(
                    () -> assertThat(isCancelled).isTrue(),
                    () -> assertThat(likeCounts.get(restaurant.getCode())).isEqualTo(0)
            );
        }

        @Test
        void 좋아요를_추가한_적_없는_식당에_좋아요를_취소할_수_없다() {
            // given
            String pickeatCode = "pickeat-code";
            String participantCode = "user-1";
            RestaurantV2 restaurant = RestaurantV2Fixture.create("마라탕");
            RestaurantsV2 restaurants = new RestaurantsV2(List.of(restaurant));
            restaurantsStorage.setupRestaurants(pickeatCode, restaurants);

            // when
            boolean isCancelled = restaurantsStorage.cancelLike(pickeatCode, participantCode, restaurant.getCode());

            // then
            Map<String, Integer> likeCounts = restaurantsStorage
                    .getAllRestaurantState(pickeatCode).likeCountByRestaurant();
            assertAll(
                    () -> assertThat(isCancelled).isFalse(),
                    () -> assertThat(likeCounts.get(restaurant.getCode())).isEqualTo(0)
            );
        }
    }
}
