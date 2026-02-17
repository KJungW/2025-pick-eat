package com.pickeat.backend.pickeat.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.pickeat.backend.global.exception.BusinessException;
import com.pickeat.backend.global.exception.ErrorCode;
import com.pickeat.backend.pickeat.application.dto.response.PickeatResultResponseV2;
import com.pickeat.backend.pickeat.domain.PickeatRecord;
import com.pickeat.backend.pickeat.domain.PickeatResultV2;
import com.pickeat.backend.pickeat.domain.PickeatV2;
import com.pickeat.backend.restaurant.domain.RestaurantV2;
import com.pickeat.backend.support.DatabaseSliceTest;
import com.pickeat.backend.support.fixture.RestaurantV2Fixture;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

@Import({PickeatResultServiceV2.class})
class PickeatResultServiceV2Test extends DatabaseSliceTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private PickeatResultServiceV2 pickeatResultService;

    @Nested
    class 픽잇_결과_조회 {

        @Test
        void 픽잇_결과를_성공적으로_조회할_수_있다() {
            // given
            PickeatV2 pickeat = PickeatV2.createWithoutRoom("테스트 픽잇");
            PickeatRecord record = testEntityManager.persist(PickeatRecord.from(pickeat));

            RestaurantV2 restaurant = RestaurantV2Fixture.create("선정된 맛집");
            PickeatResultV2 result = testEntityManager.persist(PickeatResultV2.from(record.getId(), restaurant));

            // when
            PickeatResultResponseV2 response = pickeatResultService.getByPickeatCode(record.getCode());

            // then
            assertAll(
                    () -> assertThat(response.code()).isEqualTo(restaurant.getCode()),
                    () -> assertThat(response.name()).isEqualTo(restaurant.getName())
            );
        }

        @Test
        void 존재하지_않는_픽잇_결과를_조회하려고_할_경우_예외를_발생시킨다() {
            // given
            String nonExistCode = "not_exist_code";

            // when & then
            assertThatThrownBy(() -> pickeatResultService.getByPickeatCode(nonExistCode))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(ErrorCode.PICKEAT_RECORD_NOT_FOUND.getMessage());
        }
    }
}
