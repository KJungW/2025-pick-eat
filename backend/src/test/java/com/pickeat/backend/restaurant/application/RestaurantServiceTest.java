package com.pickeat.backend.restaurant.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.pickeat.backend.global.exception.ErrorCode;
import com.pickeat.backend.global.exception.type.BusinessException;
import com.pickeat.backend.pickeat.domain.Pickeat;
import com.pickeat.backend.pickeat.domain.store.PickeatStorage;
import com.pickeat.backend.restaurant.application.dto.RestaurantStateDto;
import com.pickeat.backend.restaurant.application.dto.event.RestaurantUpdateEventRequest;
import com.pickeat.backend.restaurant.application.dto.request.RestaurantRequest;
import com.pickeat.backend.restaurant.application.dto.response.RestaurantResponse;
import com.pickeat.backend.restaurant.application.dto.response.RestaurantStateResponse;
import com.pickeat.backend.restaurant.domain.Restaurants;
import com.pickeat.backend.restaurant.domain.storage.RestaurantsStorage;
import com.pickeat.backend.support.DatabaseSliceTest;
import com.pickeat.backend.support.fixture.RestaurantFixture;
import com.pickeat.backend.support.fixture.RestaurantRequestFixture;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.event.ApplicationEvents;

@Import({RestaurantService.class, RestaurantsStorage.class, PickeatStorage.class})
class RestaurantServiceTest extends DatabaseSliceTest {

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private RestaurantsStorage restaurantsStorage;

    @Autowired
    private PickeatStorage pickeatStorage;

    @Autowired
    private ApplicationEvents events;

    @Nested
    class 식당_저장 {

        @Test
        void 식당을_성공적으로_저장한다() {
            // given
            Pickeat pickeat = Pickeat.createWithoutRoom("점심");
            pickeatStorage.save(pickeat);

            List<RestaurantRequest> requests = List.of(
                    RestaurantRequestFixture.create("마라탕"),
                    RestaurantRequestFixture.create("돈가스"));

            // when
            restaurantService.create(pickeat.getCode(), requests);

            // then
            Optional<Restaurants> saved = restaurantsStorage.getRestaurantMeta(pickeat.getCode());
            assertAll(
                    () -> assertThat(saved).isPresent(),
                    () -> assertThat(saved.get().getRestaurants()).hasSize(2),
                    () -> assertThat(saved.get().getRestaurants().get(0).getName()).isEqualTo("마라탕")
            );
        }

        @Test
        void 식당을_저장하면_식당_소거도_함께_세팅된다() {
            // given
            Pickeat pickeat = Pickeat.createWithoutRoom("점심");
            pickeatStorage.save(pickeat);

            List<RestaurantRequest> requests = List.of(
                    RestaurantRequestFixture.create("마라탕"),
                    RestaurantRequestFixture.create("돈가스"));

            // when
            restaurantService.create(pickeat.getCode(), requests);

            // then
            Optional<Restaurants> savedRestaurants = restaurantsStorage.getRestaurantMeta(pickeat.getCode());
            Set<String> aliveRestaurantCodes = restaurantsStorage.getRestaurantState(pickeat.getCode()).get()
                    .aliveRestaurantCode();

            assertAll(
                    () -> assertThat(aliveRestaurantCodes).hasSize(2),
                    () -> assertThat(aliveRestaurantCodes)
                            .containsExactlyInAnyOrderElementsOf(savedRestaurants.get().extrudeRestaurantCodes())
            );
        }

        @Test
        void 픽잇_코드에_대한_픽잇이_없는_경우_예외를_발생시킨다() {
            // given
            String invalidCode = "NON-EXISTENT-CODE";
            List<RestaurantRequest> requests = List.of(RestaurantRequestFixture.create("마라탕"));

            // when & then
            assertThatThrownBy(() -> restaurantService.create(invalidCode, requests))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(ErrorCode.PROCESSING_PICKEAT_NOT_FOUND.getMessage());
        }

        @Test
        void 픽잇에_이미_식당이_존재하는_경우_예외를_발생시킨다() {
            // given
            Pickeat pickeat = Pickeat.createWithoutRoom("점심");
            pickeatStorage.save(pickeat);

            Restaurants existing = new Restaurants(List.of(RestaurantFixture.create("기존 식당")));
            restaurantsStorage.setupRestaurants(pickeat.getCode(), existing);

            List<RestaurantRequest> newRequests = List.of(RestaurantRequestFixture.create("새로운 식당"));

            // when & then
            assertThatThrownBy(() -> restaurantService.create(pickeat.getCode(), newRequests))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(ErrorCode.RESTAURANT_ALREADY_EXISTS.getMessage());
        }
    }

    @Nested
    class 픽잇_식당_메타정보_조회 {

        @Test
        void 픽잇의_식당_메타정보를_성공적으로_조회한다() {
            // given
            Pickeat pickeat = Pickeat.createWithoutRoom("저녁 회식");
            pickeatStorage.save(pickeat);

            Restaurants restaurants = new Restaurants(List.of(
                    RestaurantFixture.create("초밥"),
                    RestaurantFixture.create("삼겹살")));
            restaurantsStorage.setupRestaurants(pickeat.getCode(), restaurants);

            // when
            List<RestaurantResponse> response = restaurantService.getMetaInPickeat(pickeat.getCode());

            // then
            assertAll(
                    () -> assertThat(response).hasSize(2),
                    () -> assertThat(response).extracting("name")
                            .containsExactlyInAnyOrder("초밥", "삼겹살")
            );
        }

        @Test
        void 픽잇이_존재하지_않는_경우_예외를_발생시킨다() {
            // given
            String invalidCode = "NON-EXISTENT-CODE";

            // when & then
            assertThatThrownBy(() -> restaurantService.getMetaInPickeat(invalidCode))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(ErrorCode.PROCESSING_PICKEAT_NOT_FOUND.getMessage());
        }
    }

    @Nested
    class 식당_상태정보_조회 {

        @Test
        void 식당의_상태정보를_성공적으로_조회한다() {
            // given
            Pickeat pickeat = Pickeat.createWithoutRoom("회식 장소 투표");
            pickeatStorage.save(pickeat);

            List<RestaurantRequest> requests = List.of(
                    RestaurantRequestFixture.create("치킨"),
                    RestaurantRequestFixture.create("피자"));
            restaurantService.create(pickeat.getCode(), requests);

            Restaurants meta = restaurantsStorage.getRestaurantMeta(pickeat.getCode()).get();
            String restaurantCode1 = meta.getRestaurants().get(0).getCode();
            String restaurantCode2 = meta.getRestaurants().get(1).getCode();

            restaurantService.like(pickeat.getCode(), "user-1", restaurantCode1);
            restaurantService.exclude(pickeat.getCode(), List.of(restaurantCode2));

            // when
            RestaurantStateResponse response = restaurantService.getStateInPickeat(pickeat.getCode());

            // then
            assertAll(
                    () -> assertThat(response.sequence()).isEqualTo(0),
                    () -> assertThat(response.aliveRestaurantCode()).hasSize(1),
                    () -> assertThat(response.aliveRestaurantCode()).containsExactly(restaurantCode1),
                    () -> assertThat(response.likeCountByRestaurant()).hasSize(2),
                    () -> assertThat(response.likeCountByRestaurant().get(restaurantCode1)).isEqualTo(1),
                    () -> assertThat(response.likeCountByRestaurant().get(restaurantCode2)).isEqualTo(0)
            );
        }

        @Test
        void 픽잇이_존재하지_않는_경우_상태정보_조회_시_예외를_발생시킨다() {
            // given
            String invalidCode = "NON-EXISTENT-CODE";

            // when & then
            assertThatThrownBy(() -> restaurantService.getStateInPickeat(invalidCode))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROCESSING_PICKEAT_NOT_FOUND);
        }
    }

    @Nested
    class 픽잇_식당_소거 {

        @Test
        void 픽잇의_식당을_성공적으로_소거한다() {
            // given
            Pickeat pickeat = Pickeat.createWithoutRoom("저녁 회식");
            pickeatStorage.save(pickeat);

            List<RestaurantRequest> restaurants = List.of(
                    RestaurantRequestFixture.create("restaurant1"),
                    RestaurantRequestFixture.create("restaurant2"));
            restaurantService.create(pickeat.getCode(), restaurants);

            List<String> restaurantCodes = restaurantsStorage.getRestaurantMeta(pickeat.getCode()).get()
                    .extrudeRestaurantCodes();

            // when
            restaurantService.exclude(pickeat.getCode(), restaurantCodes);

            // then
            RestaurantStateDto restaurantState = restaurantsStorage.getRestaurantState(pickeat.getCode())
                    .get();
            assertThat(restaurantState.aliveRestaurantCode()).isEmpty();
        }

        @Test
        void 식당_상태_업데이트_이벤트를_발행한다() {
            // given
            Pickeat pickeat = Pickeat.createWithoutRoom("저녁 회식");
            pickeatStorage.save(pickeat);

            List<RestaurantRequest> requests = List.of(RestaurantRequestFixture.create("restaurant1"));
            restaurantService.create(pickeat.getCode(), requests);

            Restaurants meta = restaurantsStorage.getRestaurantMeta(pickeat.getCode()).get();
            List<String> restaurantCodes = meta.extrudeRestaurantCodes();

            // when
            restaurantService.exclude(pickeat.getCode(), restaurantCodes);

            // then
            Long eventCount = events.stream(RestaurantUpdateEventRequest.class)
                    .filter(event -> event.pickeatCode().equals(pickeat.getCode()))
                    .count();
            assertThat(eventCount).isEqualTo(1);
        }

        @Test
        void 픽잇에_식당이_존재하지_않는_경우_예외를_발생시킨다() {
            // given
            String invalidPickeatCode = "invalid-code";
            List<String> restaurantCodes = List.of("RES001", "RES002");

            // when & then
            assertThatThrownBy(() -> restaurantService.exclude(invalidPickeatCode, restaurantCodes))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROCESSING_PICKEAT_NOT_FOUND);
        }
    }

    @Nested
    class 식당_좋아요 {

        @Test
        void 식당에_좋아요를_성공적으로_추가할_수_있다() {
            // given
            Pickeat pickeat = Pickeat.createWithoutRoom("점심 메뉴 결정");
            pickeatStorage.save(pickeat);

            List<RestaurantRequest> requests = List.of(RestaurantRequestFixture.create("마라탕"));
            restaurantService.create(pickeat.getCode(), requests);

            String restaurantCode = restaurantsStorage.getRestaurantMeta(pickeat.getCode())
                    .get().getRestaurants().get(0).getCode();
            String participantCode = "user-123";

            // when
            restaurantService.like(pickeat.getCode(), participantCode, restaurantCode);

            // then
            RestaurantStateDto restaurantState = restaurantsStorage.getRestaurantState(pickeat.getCode())
                    .get();
            assertThat(restaurantState.likeCountByRestaurant().get(restaurantCode)).isEqualTo(1);
        }

        @Test
        void 이미_좋아요를_추가한_식당에_좋아요를_추가할_수_없다() {
            // given
            Pickeat pickeat = Pickeat.createWithoutRoom("점심 메뉴 결정");
            pickeatStorage.save(pickeat);

            List<RestaurantRequest> requests = List.of(RestaurantRequestFixture.create("마라탕"));
            restaurantService.create(pickeat.getCode(), requests);

            String restaurantCode = restaurantsStorage.getRestaurantMeta(pickeat.getCode())
                    .get().getRestaurants().get(0).getCode();
            String participantCode = "user-123";
            restaurantService.like(pickeat.getCode(), participantCode, restaurantCode);

            // when & then
            assertThatThrownBy(() -> restaurantService.like(pickeat.getCode(), participantCode, restaurantCode))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PARTICIPANT_RESTAURANT_ALREADY_LIKED);
        }

        @Test
        void 식당_상태_업데이트_이벤트를_발행한다() {
            // given
            Pickeat pickeat = Pickeat.createWithoutRoom("점심 메뉴 결정");
            pickeatStorage.save(pickeat);

            List<RestaurantRequest> requests = List.of(RestaurantRequestFixture.create("마라탕"));
            restaurantService.create(pickeat.getCode(), requests);

            String restaurantCode = restaurantsStorage.getRestaurantMeta(pickeat.getCode())
                    .get().getRestaurants().get(0).getCode();

            // when
            restaurantService.like(pickeat.getCode(), "user-1", restaurantCode);

            // then
            Long eventCount = events.stream(RestaurantUpdateEventRequest.class)
                    .filter(event -> event.pickeatCode().equals(pickeat.getCode()))
                    .count();
            assertThat(eventCount).isEqualTo(1);
        }
    }

    @Nested
    class 식당_좋아요_취소 {

        @Test
        void 식당에_좋아요를_성공적으로_취소할_수_있다() {
            // given
            Pickeat pickeat = Pickeat.createWithoutRoom("저녁 메뉴 결정");
            pickeatStorage.save(pickeat);

            List<RestaurantRequest> requests = List.of(RestaurantRequestFixture.create("삼겹살"));
            restaurantService.create(pickeat.getCode(), requests);

            String restaurantCode = restaurantsStorage.getRestaurantMeta(pickeat.getCode())
                    .get().getRestaurants().get(0).getCode();
            String participantCode = "user-123";
            restaurantService.like(pickeat.getCode(), participantCode, restaurantCode);

            // when
            restaurantService.cancelLike(pickeat.getCode(), participantCode, restaurantCode);

            // then
            RestaurantStateDto restaurantState = restaurantsStorage.getRestaurantState(pickeat.getCode())
                    .get();
            assertThat(restaurantState.likeCountByRestaurant().get(restaurantCode)).isEqualTo(0);
        }

        @Test
        void 좋아요를_추가한_적_없는_식당에_좋아요를_취소할_수_없다() {
            // given
            Pickeat pickeat = Pickeat.createWithoutRoom("저녁 메뉴 결정");
            pickeatStorage.save(pickeat);

            List<RestaurantRequest> requests = List.of(RestaurantRequestFixture.create("삼겹살"));
            restaurantService.create(pickeat.getCode(), requests);

            String restaurantCode = restaurantsStorage.getRestaurantMeta(pickeat.getCode())
                    .get().getRestaurants().get(0).getCode();
            String participantCode = "user-123";

            // when & then
            assertThatThrownBy(() -> restaurantService.cancelLike(pickeat.getCode(), participantCode, restaurantCode))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PARTICIPANT_RESTAURANT_NOT_LIKED);
        }

        @Test
        void 식당_상태_업데이트_이벤트를_발행한다() {
            // given
            Pickeat pickeat = Pickeat.createWithoutRoom("저녁 메뉴 결정");
            pickeatStorage.save(pickeat);

            List<RestaurantRequest> requests = List.of(RestaurantRequestFixture.create("삼겹살"));
            restaurantService.create(pickeat.getCode(), requests);

            String restaurantCode = restaurantsStorage.getRestaurantMeta(pickeat.getCode())
                    .get().getRestaurants().get(0).getCode();
            String participantCode = "user-123";
            restaurantService.like(pickeat.getCode(), participantCode, restaurantCode);

            // when
            restaurantService.cancelLike(pickeat.getCode(), participantCode, restaurantCode);

            // then
            Long eventCount = events.stream(RestaurantUpdateEventRequest.class)
                    .filter(event -> event.pickeatCode().equals(pickeat.getCode()))
                    .count();
            assertThat(eventCount).isEqualTo(2); // 좋아요 시 1번, 취소 시 1번 총 2번의 이벤트가 발행됨
        }
    }
}
