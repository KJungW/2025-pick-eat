package com.pickeat.backend.restaurant.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.pickeat.backend.global.exception.BusinessException;
import com.pickeat.backend.global.exception.ErrorCode;
import com.pickeat.backend.pickeat.domain.PickeatV2;
import com.pickeat.backend.pickeat.domain.store.PickeatStorage;
import com.pickeat.backend.restaurant.application.dto.request.RestaurantRequest;
import com.pickeat.backend.restaurant.application.dto.response.RestaurantResponseV2;
import com.pickeat.backend.restaurant.domain.RestaurantsV2;
import com.pickeat.backend.restaurant.domain.storage.RestaurantsStorage;
import com.pickeat.backend.support.DatabaseSliceTest;
import com.pickeat.backend.support.fixture.RestaurantRequestFixture;
import com.pickeat.backend.support.fixture.RestaurantV2Fixture;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({RestaurantServiceV2.class, RestaurantsStorage.class, PickeatStorage.class})
class RestaurantServiceV2Test extends DatabaseSliceTest {

    @Autowired
    private RestaurantServiceV2 restaurantService;

    @Autowired
    private RestaurantsStorage restaurantsStorage;

    @Autowired
    private PickeatStorage pickeatStorage;

    @Nested
    class 식당_저장 {

        @Test
        void 식당을_성공적으로_저장한다() {
            // given
            PickeatV2 pickeat = PickeatV2.createWithoutRoom("점심");
            pickeatStorage.save(pickeat);

            List<RestaurantRequest> requests = List.of(
                    RestaurantRequestFixture.create("마라탕"),
                    RestaurantRequestFixture.create("돈가스"));

            // when
            restaurantService.create(requests, pickeat.getCode());

            // then
            Optional<RestaurantsV2> saved = restaurantsStorage.get(pickeat.getCode());
            assertAll(
                    () -> assertThat(saved).isPresent(),
                    () -> assertThat(saved.get().getRestaurants()).hasSize(2),
                    () -> assertThat(saved.get().getRestaurants().get(0).getName()).isEqualTo("마라탕")
            );
        }

        @Test
        void 픽잇_코드에_대한_픽잇이_없는_경우_예외를_발생시킨다() {
            // given
            String invalidCode = "NON-EXISTENT-CODE";
            List<RestaurantRequest> requests = List.of(RestaurantRequestFixture.create("마라탕"));

            // when & then
            assertThatThrownBy(() -> restaurantService.create(requests, invalidCode))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(ErrorCode.PICKEAT_NOT_FOUND.getMessage());
        }

        @Test
        void 픽잇에_이미_식당이_존재하는_경우_예외를_발생시킨다() {
            // given
            PickeatV2 pickeat = PickeatV2.createWithoutRoom("점심");
            pickeatStorage.save(pickeat);

            RestaurantsV2 existing = new RestaurantsV2(List.of(RestaurantV2Fixture.create("기존 식당")));
            restaurantsStorage.saveIfAbsent(existing, pickeat.getCode());

            List<RestaurantRequest> newRequests = List.of(RestaurantRequestFixture.create("새로운 식당"));

            // when & then
            assertThatThrownBy(() -> restaurantService.create(newRequests, pickeat.getCode()))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(ErrorCode.RESTAURANT_ALREADY_EXISTS.getMessage());
        }
    }

    @Nested
    class 픽잇_식당_조회 {

        @Test
        void 픽잇의_식당을_성공적으로_조회한다() {
            // given
            PickeatV2 pickeat = PickeatV2.createWithoutRoom("저녁 회식");
            pickeatStorage.save(pickeat);

            RestaurantsV2 restaurants = new RestaurantsV2(List.of(
                    RestaurantV2Fixture.create("초밥"),
                    RestaurantV2Fixture.create("삼겹살")));
            restaurantsStorage.saveIfAbsent(restaurants, pickeat.getCode());

            // when
            List<RestaurantResponseV2> response = restaurantService.getByPickeat(pickeat.getCode());

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
            assertThatThrownBy(() -> restaurantService.getByPickeat(invalidCode))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(ErrorCode.PICKEAT_NOT_FOUND.getMessage());
        }

        @Test
        void 픽잇에_식당이_존재하지_않는_경우_예외를_발생시킨다() {
            // given
            PickeatV2 pickeat = PickeatV2.createWithoutRoom("빈 픽잇");
            pickeatStorage.save(pickeat);

            // when & then
            assertThatThrownBy(() -> restaurantService.getByPickeat(pickeat.getCode()))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(ErrorCode.RESTAURANT_NOT_FOUND.getMessage());
        }
    }
}
