package com.pickeat.backend.pickeat.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.pickeat.backend.participant.domain.ParticipantV2;
import com.pickeat.backend.participant.domain.storage.ParticipantStorage;
import com.pickeat.backend.pickeat.application.dto.request.PickeatRequest;
import com.pickeat.backend.pickeat.application.dto.response.PickeatResponseV2;
import com.pickeat.backend.pickeat.domain.PickeatRecord;
import com.pickeat.backend.pickeat.domain.PickeatResultV2;
import com.pickeat.backend.pickeat.domain.PickeatV2;
import com.pickeat.backend.pickeat.domain.repository.PickeatRecordRepository;
import com.pickeat.backend.pickeat.domain.repository.PickeatResultRepositoryV2;
import com.pickeat.backend.pickeat.domain.store.PickeatStorage;
import com.pickeat.backend.restaurant.domain.RestaurantV2;
import com.pickeat.backend.restaurant.domain.RestaurantsV2;
import com.pickeat.backend.restaurant.domain.storage.RestaurantsStorage;
import com.pickeat.backend.room.domain.Room;
import com.pickeat.backend.room.domain.RoomUser;
import com.pickeat.backend.support.DatabaseSliceTest;
import com.pickeat.backend.support.fixture.RestaurantV2Fixture;
import com.pickeat.backend.support.fixture.RoomFixture;
import com.pickeat.backend.support.fixture.UserFixture;
import com.pickeat.backend.user.domain.User;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

@Import({PickeatServiceV2.class, PickeatStorage.class, RestaurantsStorage.class, ParticipantStorage.class})
class PickeatServiceV2Test extends DatabaseSliceTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private PickeatStorage pickeatStorage;

    @Autowired
    private RestaurantsStorage restaurantsStorage;

    @Autowired
    private ParticipantStorage participantStorage;

    @Autowired
    private PickeatRecordRepository pickeatRecordRepository;

    @Autowired
    private PickeatResultRepositoryV2 pickeatResultRepository;

    @Autowired
    private PickeatServiceV2 pickeatService;

    @Nested
    class 픽잇_생성 {

        @Test
        void 외부용_픽잇_생성_성공() {
            // given
            PickeatRequest pickeatRequest = new PickeatRequest("픽잇");

            // when
            PickeatResponseV2 response = pickeatService.createPickeatWithoutRoom(pickeatRequest);

            // then
            Optional<PickeatV2> pickeat = pickeatStorage.get(response.code());
            assertAll(
                    () -> assertThat(pickeat.isPresent()).isTrue(),
                    () -> assertThat(pickeat.get().getCode()).isEqualTo(response.code())
            );
        }

        @Test
        void 방_내부용_픽잇_생성_성공() {
            // given
            Room room = testEntityManager.persist(RoomFixture.create());
            User user = testEntityManager.persist(UserFixture.create());
            testEntityManager.persist(new RoomUser(room.getId(), user.getId()));

            PickeatRequest pickeatRequest = new PickeatRequest("픽잇");

            // when
            PickeatResponseV2 response = pickeatService.createPickeatWithRoom(
                    room.getId(), user.getId(), pickeatRequest);

            // then
            Optional<PickeatV2> pickeat = pickeatStorage.get(response.code());
            assertAll(
                    () -> assertThat(pickeat.isPresent()).isTrue(),
                    () -> assertThat(pickeat.get().getCode()).isEqualTo(response.code())
            );
        }
    }


    @Nested
    class 픽잇_종료 {

        @Test
        void 픽잇_결과를_DB에_저장할_수_있다() {
            // given
            PickeatV2 pickeat = PickeatV2.createWithoutRoom("테스트 픽잇");
            pickeatStorage.save(pickeat);

            RestaurantV2 restaurantA = RestaurantV2Fixture.create("식당A");
            RestaurantV2 restaurantB = RestaurantV2Fixture.create("식당B");
            RestaurantsV2 restaurants = new RestaurantsV2(List.of(restaurantA, restaurantB));
            restaurantsStorage.setupRestaurants(pickeat.getCode(), restaurants);

            ParticipantV2 participant = new ParticipantV2("참가자");
            participantStorage.setupAboutParticipant(pickeat.getCode(), participant);

            restaurantsStorage.like(pickeat.getCode(), participant.getCode(), restaurantA.getCode());

            // when
            pickeatService.completePickeat(pickeat.getCode());

            // then
            PickeatRecord pickeatRecord = pickeatRecordRepository.findAll().get(0);
            PickeatResultV2 pickeatResult = pickeatResultRepository.findAll().get(0);

            assertAll(
                    () -> assertThat(pickeatRecord.getCode()).isEqualTo(pickeat.getCode()),
                    () -> assertThat(pickeatResult.getPickeatRecordId()).isEqualTo(pickeatRecord.getId()),
                    () -> assertThat(pickeatResult.getCode()).isEqualTo(restaurantA.getCode())
            );
        }

        @Test
        void 픽잇_관련_스토리지_데이터를_제거할_수_있다() {
            // given
            PickeatV2 pickeat = PickeatV2.createWithoutRoom("테스트 픽잇");
            String code = pickeat.getCode();
            pickeatStorage.save(pickeat);

            RestaurantV2 restaurant = RestaurantV2Fixture.create("식당");
            restaurantsStorage.setupRestaurants(code, new RestaurantsV2(List.of(restaurant)));

            ParticipantV2 participant = new ParticipantV2("참가자");
            participantStorage.setupAboutParticipant(code, participant);

            // when
            pickeatService.completePickeat(code);

            // then
            assertAll(
                    () -> assertThat(pickeatStorage.get(code).isEmpty()).isTrue(),
                    () -> assertThat(restaurantsStorage.getAllRestaurantMeta(code).isEmpty()).isTrue(),
                    () -> assertThat(participantStorage.getParticipantsMeta(code).isEmpty()).isTrue()
            );
        }
    }
}
