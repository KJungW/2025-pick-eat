package com.pickeat.backend.pickeat.domain.store;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.pickeat.backend.global.setting.StorageKey;
import com.pickeat.backend.pickeat.domain.PickeatV2;
import com.pickeat.backend.support.DatabaseSliceTest;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;

@Import(PickeatStorage.class)
class PickeatStorageTest extends DatabaseSliceTest {

    @Autowired
    private PickeatStorage pickeatStorage;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Nested
    class 픽잇_저장 {

        @Test
        void Pickeat을_성공적으로_저장한다() {
            // given
            PickeatV2 pickeat = PickeatV2.createWithoutRoom("점식메뉴");
            String expectedKey = StorageKey.PICKEAT.generateKey(pickeat.getCode());

            // when
            pickeatStorage.save(pickeat);

            // then
            PickeatV2 saved = (PickeatV2) redisTemplate.opsForValue().get(expectedKey);
            assertAll(
                    () -> assertThat(saved).isNotNull(),
                    () -> assertThat(saved.getCode()).isEqualTo(pickeat.getCode())
            );
        }
    }

    @Nested
    class 픽잇_조회 {

        @Test
        void Pickeat을_성공적으로_조회한다() {
            // given
            PickeatV2 pickeat = PickeatV2.createWithoutRoom("점식메뉴");
            pickeatStorage.save(pickeat);

            // when
            Optional<PickeatV2> result = pickeatStorage.get(pickeat.getCode());

            // then
            assertAll(
                    () -> assertThat(result).isPresent(),
                    () -> assertThat(result.get().getCode()).isEqualTo(pickeat.getCode())
            );
        }

        @Test
        void 픽잇이_존재하지_않는_경우_빈_Optional이_반환된다() {
            // given
            String nonExistentCode = "NOT_FOUND";

            // when
            Optional<PickeatV2> result = pickeatStorage.get(nonExistentCode);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    class 픽잇_제거 {

        @Test
        void 픽잇과을_제거할_수_있다() {
            // given
            PickeatV2 pickeat = PickeatV2.createWithoutRoom("제거 테스트 픽잇");
            String code = pickeat.getCode();
            pickeatStorage.save(pickeat);

            // when
            pickeatStorage.remove(code);

            // then
            Optional<PickeatV2> result = pickeatStorage.get(code);
            assertThat(result).isEmpty();
        }
    }
}
