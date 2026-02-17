package com.pickeat.backend.pickeat.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.pickeat.backend.global.exception.BusinessException;
import com.pickeat.backend.global.exception.ErrorCode;
import com.pickeat.backend.pickeat.application.dto.response.PickeatResultResponse;
import com.pickeat.backend.pickeat.domain.Pickeat;
import com.pickeat.backend.pickeat.domain.PickeatRecord;
import com.pickeat.backend.pickeat.domain.PickeatResult;
import com.pickeat.backend.restaurant.domain.Restaurant;
import com.pickeat.backend.support.DatabaseSliceTest;
import com.pickeat.backend.support.fixture.RestaurantFixture;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

@Import({PickeatResultService.class})
class PickeatResultServiceTest extends DatabaseSliceTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private PickeatResultService pickeatResultService;

    @Nested
    class 픽잇_결과_조회 {

        @Test
        void 픽잇_결과를_성공적으로_조회할_수_있다() {
            // given
            Pickeat pickeat = Pickeat.createWithoutRoom("테스트 픽잇");
            PickeatRecord record = testEntityManager.persist(PickeatRecord.from(pickeat));

            Restaurant restaurant = RestaurantFixture.create("선정된 맛집");
            PickeatResult result = testEntityManager.persist(PickeatResult.from(record.getId(), restaurant));

            // when
            PickeatResultResponse response = pickeatResultService.getByPickeatCode(record.getCode());

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
