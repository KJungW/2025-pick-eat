package com.pickeat.backend.restaurant.domain.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.pickeat.backend.global.configuration.storage.StorageKey;
import com.pickeat.backend.global.utility.JsonParser;
import com.pickeat.backend.restaurant.application.dto.RestaurantStateDto;
import com.pickeat.backend.restaurant.domain.Restaurant;
import com.pickeat.backend.restaurant.domain.Restaurants;
import com.pickeat.backend.support.DatabaseSliceTest;
import com.pickeat.backend.support.fixture.RestaurantFixture;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;

@Import(RestaurantsCommandStorage.class)
class RestaurantsCommandStorageTest extends DatabaseSliceTest {

    @Autowired
    private RestaurantsCommandStorage restaurantsCommandStorage;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private JsonParser jsonParser;

    @Nested
    class 식당_관련_데이터_세팅 {

        @Test
        void 식당_메타데이터를_성공적으로_세팅한다() {
            // given
            Restaurants restaurants = new Restaurants(List.of(
                    RestaurantFixture.create("마라탕"),
                    RestaurantFixture.create("돈가스")));
            String pickeatCode = "pickeat-code";

            // when
            Boolean isSuccess = restaurantsCommandStorage.setupRestaurants(pickeatCode, restaurants);

            // then
            Restaurants savedMeta = restaurantsCommandStorage.getRestaurantMeta(pickeatCode).get();
            assertAll(
                    () -> assertThat(isSuccess).isTrue(),
                    () -> assertThat(savedMeta.getRestaurants()).hasSize(2),
                    () -> assertThat(savedMeta.getRestaurants())
                            .extracting(Restaurant::getName)
                            .containsExactlyInAnyOrder("마라탕", "돈가스")
            );
        }

        @Test
        void 생존_식당_코드목록을_성공적으로_세팅한다() {
            // given
            Restaurants restaurants = new Restaurants(List.of(
                    RestaurantFixture.create("마라탕"),
                    RestaurantFixture.create("돈가스")));
            String pickeatCode = "pickeat-code";

            // when
            Boolean isSuccess = restaurantsCommandStorage.setupRestaurants(pickeatCode, restaurants);

            // then
            List<String> restaurantCodes = restaurants.extrudeRestaurantCodes();
            Set<String> aliveCodes = restaurantsCommandStorage.getRestaurantState(pickeatCode).get()
                    .aliveRestaurantCode();
            assertAll(
                    () -> assertThat(isSuccess).isTrue(),
                    () -> assertThat(aliveCodes).hasSize(2),
                    () -> assertThat(aliveCodes).containsExactlyInAnyOrderElementsOf(restaurantCodes)
            );
        }

        @Test
        void 식당_좋아요_합계를_성공적으로_세팅한다() {
            // given
            Restaurants restaurants = new Restaurants(List.of(
                    RestaurantFixture.create("마라탕"),
                    RestaurantFixture.create("돈가스")));
            String pickeatCode = "pickeat-code";

            // when
            Boolean isSuccess = restaurantsCommandStorage.setupRestaurants(pickeatCode, restaurants);

            // then
            List<String> restaurantCodes = restaurants.extrudeRestaurantCodes();
            Map<String, Integer> savedLikeCount = restaurantsCommandStorage.getRestaurantState(pickeatCode).get()
                    .likeCountByRestaurant();
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
            Restaurants existing = new Restaurants(List.of(RestaurantFixture.create("기존 데이터")));
            String pickeatCode = "pickeat-code";
            restaurantsCommandStorage.setupRestaurants(pickeatCode, existing);

            Restaurants newData = new Restaurants(List.of(RestaurantFixture.create("새 데이터")));

            // when
            Boolean result = restaurantsCommandStorage.setupRestaurants(pickeatCode, newData);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    class 식당_메타정보_조회 {

        @Test
        void 식당의_메타정보를_성공적으로_조회한다() {
            // given
            Restaurants restaurants = new Restaurants(List.of(RestaurantFixture.create("삼겹살")));
            String pickeatCode = "pickeat-code";
            restaurantsCommandStorage.setupRestaurants(pickeatCode, restaurants);

            // when
            Optional<Restaurants> result = restaurantsCommandStorage.getRestaurantMeta(pickeatCode);

            // then
            assertAll(
                    () -> assertThat(result).isPresent(),
                    () -> assertThat(result.get().getRestaurants().get(0).getName()).isEqualTo("삼겹살")
            );
        }

        @Test
        void 조회할_식당이_없는_경우_비어있는_Optional을_리턴한다() {
            // given
            String nonExistentCode = "EMPTY_CODE";

            // when
            Optional<Restaurants> result = restaurantsCommandStorage.getRestaurantMeta(nonExistentCode);

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
            Restaurant restaurant1 = RestaurantFixture.create("마라탕");
            Restaurant restaurant2 = RestaurantFixture.create("돈가스");
            Restaurants restaurants = new Restaurants(List.of(restaurant1, restaurant2));

            restaurantsCommandStorage.setupRestaurants(pickeatCode, restaurants);

            restaurantsCommandStorage.like(pickeatCode, "user-1", restaurant1.getCode());
            restaurantsCommandStorage.like(pickeatCode, "user-2", restaurant1.getCode());
            restaurantsCommandStorage.excludeRestaurants(pickeatCode, List.of(restaurant2.getCode()));

            // when
            Optional<RestaurantStateDto> result = restaurantsCommandStorage.getRestaurantState(pickeatCode);

            // then
            assertAll(
                    () -> assertThat(result).isPresent(),
                    () -> assertThat(result.get().sequence()).isEqualTo(0L),
                    () -> assertThat(result.get().aliveRestaurantCode()).hasSize(1),
                    () -> assertThat(result.get().aliveRestaurantCode()).containsExactly(restaurant1.getCode()),
                    () -> assertThat(result.get().likeCountByRestaurant()).hasSize(2),
                    () -> assertThat(result.get().likeCountByRestaurant().get(restaurant1.getCode())).isEqualTo(2),
                    () -> assertThat(result.get().likeCountByRestaurant().get(restaurant2.getCode())).isEqualTo(0)
            );
        }

        @Test
        void 조회할_식당이_없는_경우_비어있는_Optional을_리턴한다() {
            // given
            String nonExistentCode = "EMPTY_CODE";

            // when
            Optional<RestaurantStateDto> result = restaurantsCommandStorage.getRestaurantState(nonExistentCode);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    class 식당_상태정보_조회후_시퀀스_증가 {

        @Test
        void 식당의_상태정보를_성공적으로_조회한다() {
            // given
            String pickeatCode = "seq-test-code";
            Restaurant restaurant = RestaurantFixture.create("마라탕");
            Restaurants restaurants = new Restaurants(List.of(restaurant));
            restaurantsCommandStorage.setupRestaurants(pickeatCode, restaurants);

            // when
            Optional<RestaurantStateDto> result = restaurantsCommandStorage.getRestaurantStateWithSequence(pickeatCode);

            // then
            assertAll(
                    () -> assertThat(result).isPresent(),
                    () -> assertThat(result.get().aliveRestaurantCode()).containsExactly(restaurant.getCode()),
                    () -> assertThat(result.get().likeCountByRestaurant().get(restaurant.getCode())).isEqualTo(0)
            );
        }

        @Test
        void 식당_상태_관련_시퀀스_번호를_성공적으로_조회한다() {
            // given
            String pickeatCode = "seq-incr-code";
            Restaurants restaurants = new Restaurants(List.of(RestaurantFixture.create("마라탕")));
            restaurantsCommandStorage.setupRestaurants(pickeatCode, restaurants);

            // when
            Optional<RestaurantStateDto> firstCall = restaurantsCommandStorage.getRestaurantStateWithSequence(
                    pickeatCode);
            Optional<RestaurantStateDto> secondCall = restaurantsCommandStorage.getRestaurantStateWithSequence(
                    pickeatCode);
            Optional<RestaurantStateDto> thirdCall = restaurantsCommandStorage.getRestaurantStateWithSequence(
                    pickeatCode);

            // then
            assertAll(
                    () -> assertThat(firstCall.get().sequence()).isEqualTo(1L),
                    () -> assertThat(secondCall.get().sequence()).isEqualTo(2L),
                    () -> assertThat(thirdCall.get().sequence()).isEqualTo(3L)
            );
        }

        @Test
        void 시퀀스_번호를_처음_생성할때_TTL을_설정한다() {
            // given
            String pickeatCode = "ttl-test-code";
            String sequenceKey = StorageKey.RESTAURANT_SEQUENCE.generateKey(pickeatCode);
            Restaurants restaurants = new Restaurants(List.of(RestaurantFixture.create("마라탕")));

            restaurantsCommandStorage.setupRestaurants(pickeatCode, restaurants);
            Duration expectedTtl = StorageKey.PICKEAT_TTL;

            // when
            restaurantsCommandStorage.getRestaurantStateWithSequence(pickeatCode);

            // then
            Long actualTtl = redisTemplate.getExpire(sequenceKey);
            assertAll(
                    () -> assertThat(actualTtl).isNotNull(),
                    () -> assertThat(actualTtl).isGreaterThan(0),
                    () -> assertThat(actualTtl).isLessThanOrEqualTo(expectedTtl.getSeconds())
            );
        }

        @Test
        void 조회할_식당이_없는_경우_비어있는_Optional을_리턴한다() {
            // given
            String nonExistentCode = "EMPTY_CODE";

            // when
            Optional<RestaurantStateDto> result = restaurantsCommandStorage.getRestaurantStateWithSequence(
                    nonExistentCode);

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
            Restaurants restaurants = new Restaurants(List.of(
                    RestaurantFixture.create("마라탕"),
                    RestaurantFixture.create("돈가스"),
                    RestaurantFixture.create("쌀국수")));
            restaurantsCommandStorage.setupRestaurants(pickeatCode, restaurants);

            List<String> restaurantsCodes = restaurants.extrudeRestaurantCodes();

            // when
            restaurantsCommandStorage.excludeRestaurants(pickeatCode,
                    List.of(restaurantsCodes.get(0), restaurantsCodes.get(1)));

            // then
            Set<String> remainCodes = restaurantsCommandStorage.getRestaurantState(pickeatCode).get()
                    .aliveRestaurantCode();
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
            Restaurant restaurant = RestaurantFixture.create("마라탕");
            Restaurants restaurants = new Restaurants(List.of(restaurant));
            restaurantsCommandStorage.setupRestaurants(pickeatCode, restaurants);

            // when
            Boolean isSuccess = restaurantsCommandStorage.like(pickeatCode, participantCode, restaurant.getCode());

            // then
            Map<String, Integer> likeCounts = restaurantsCommandStorage.getRestaurantState(pickeatCode).get()
                    .likeCountByRestaurant();
            assertAll(
                    () -> assertThat(isSuccess).isTrue(),
                    () -> assertThat(likeCounts.get(restaurant.getCode())).isEqualTo(1)
            );
        }

        @Test
        void 이미_좋아요를_추가한_식당에_좋아요를_추가할_수_없다() {
            // given
            String pickeatCode = "pickeat-code";
            String participantCode = "user-1";
            Restaurant restaurant = RestaurantFixture.create("마라탕");
            Restaurants restaurants = new Restaurants(List.of(restaurant));
            restaurantsCommandStorage.setupRestaurants(pickeatCode, restaurants);
            restaurantsCommandStorage.like(pickeatCode, participantCode, restaurant.getCode());

            // when
            Boolean isSuccess = restaurantsCommandStorage.like(pickeatCode, participantCode, restaurant.getCode());

            // then
            Map<String, Integer> likeCounts = restaurantsCommandStorage.getRestaurantState(pickeatCode).get()
                    .likeCountByRestaurant();
            assertAll(
                    () -> assertThat(isSuccess).isFalse(),
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
            Restaurant restaurant = RestaurantFixture.create("마라탕");
            Restaurants restaurants = new Restaurants(List.of(restaurant));
            restaurantsCommandStorage.setupRestaurants(pickeatCode, restaurants);
            restaurantsCommandStorage.like(pickeatCode, participantCode, restaurant.getCode());

            // when
            Boolean isSuccess = restaurantsCommandStorage.cancelLike(pickeatCode, participantCode,
                    restaurant.getCode());

            // then
            Map<String, Integer> likeCounts = restaurantsCommandStorage.getRestaurantState(pickeatCode).get()
                    .likeCountByRestaurant();
            assertAll(
                    () -> assertThat(isSuccess).isTrue(),
                    () -> assertThat(likeCounts.get(restaurant.getCode())).isEqualTo(0)
            );
        }

        @Test
        void 좋아요를_추가한_적_없는_식당에_좋아요를_취소할_수_없다() {
            // given
            String pickeatCode = "pickeat-code";
            String participantCode = "user-1";
            Restaurant restaurant = RestaurantFixture.create("마라탕");
            Restaurants restaurants = new Restaurants(List.of(restaurant));
            restaurantsCommandStorage.setupRestaurants(pickeatCode, restaurants);

            // when
            Boolean isSuccess = restaurantsCommandStorage.cancelLike(pickeatCode, participantCode,
                    restaurant.getCode());

            // then
            Map<String, Integer> likeCounts = restaurantsCommandStorage.getRestaurantState(pickeatCode).get()
                    .likeCountByRestaurant();
            assertAll(
                    () -> assertThat(isSuccess).isFalse(),
                    () -> assertThat(likeCounts.get(restaurant.getCode())).isEqualTo(0)
            );
        }
    }

    @Nested
    class 픽잇_식당_관련_데이터_제거 {

        @Test
        void 픽잇과_관련된_모든_식당_데이터를_제거할_수_있다() {
            // given
            String pickeatCode = "remove-test-code";
            String participantCode = "user-1";
            Restaurant restaurant = RestaurantFixture.create("삭제될 식당");
            Restaurants restaurants = new Restaurants(List.of(restaurant));

            restaurantsCommandStorage.setupRestaurants(pickeatCode, restaurants);
            restaurantsCommandStorage.like(pickeatCode, participantCode, restaurant.getCode());
            restaurantsCommandStorage.getRestaurantStateWithSequence(pickeatCode);

            String metaKey = StorageKey.RESTAURANT_META.generateKey(pickeatCode);
            String aliveKey = StorageKey.RESTAURANT_ALIVE.generateKey(pickeatCode);
            String likeCountKey = StorageKey.RESTAURANT_LIKE_COUNT.generateKey(pickeatCode);
            String likeRecordKey = StorageKey.RESTAURANT_LIKE_RECORD.generateKey(pickeatCode, restaurant.getCode());
            String sequenceKey = StorageKey.RESTAURANT_SEQUENCE.generateKey(pickeatCode);

            // 각 키가 존재하는지 사전 검증
            assertAll(
                    () -> assertThat(redisTemplate.hasKey(metaKey)).isTrue(),
                    () -> assertThat(redisTemplate.hasKey(aliveKey)).isTrue(),
                    () -> assertThat(redisTemplate.hasKey(likeCountKey)).isTrue(),
                    () -> assertThat(redisTemplate.hasKey(likeRecordKey)).isTrue(),
                    () -> assertThat(redisTemplate.hasKey(sequenceKey)).isTrue()
            );

            // when
            restaurantsCommandStorage.remove(pickeatCode);

            // then
            assertAll(
                    () -> assertThat(redisTemplate.hasKey(metaKey)).isFalse(),
                    () -> assertThat(redisTemplate.hasKey(aliveKey)).isFalse(),
                    () -> assertThat(redisTemplate.hasKey(likeCountKey)).isFalse(),
                    () -> assertThat(redisTemplate.hasKey(likeRecordKey)).isFalse(),
                    () -> assertThat(redisTemplate.hasKey(sequenceKey)).isFalse(),
                    () -> assertThat(restaurantsCommandStorage.getRestaurantMeta(pickeatCode)).isEmpty(),
                    () -> assertThat(restaurantsCommandStorage.getRestaurantState(pickeatCode)).isEmpty()
            );
        }
    }
}
