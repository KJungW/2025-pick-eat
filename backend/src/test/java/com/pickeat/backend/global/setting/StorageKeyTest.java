package com.pickeat.backend.global.setting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pickeat.backend.global.exception.BusinessException;
import com.pickeat.backend.global.exception.ErrorCode;
import com.pickeat.backend.pickeat.domain.Participant;
import com.pickeat.backend.pickeat.domain.Pickeat;
import com.pickeat.backend.support.fixture.PickeatFixture;
import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class StorageKeyTest {

    @Nested
    class STORAGE_키_생성 {

        @Test
        void 정상적인_인자가_전달되면_포맷에_맞는_키를_생성한다() {
            // given
            String nickname = "테스트유저";
            Pickeat pickeat = PickeatFixture.createWithoutRoom();

            // when
            Participant participant = new Participant(nickname, pickeat.getId());

            // then
            assertThat(participant)
                    .extracting(
                            Participant::getNickname,
                            Participant::getPickeatId,
                            Participant::getIsCompleted)
                    .containsExactly(nickname, pickeat.getId(), false);
        }

        @ParameterizedTest
        @MethodSource("provideInvalidArgs")
        void 인자_개수가_맞지_않으면_예외가_발생한다(Object[] args, String expectedMessage) {
            // given
            StorageKey keyType = StorageKey.PICKEAT;

            // when & then
            assertThatThrownBy(() -> keyType.generateKey(args))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(ErrorCode.INVALID_STORAGE_KEY_ARGUMENT_COUNT.getMessage());
        }

        private static Stream<Arguments> provideInvalidArgs() {
            return Stream.of(
                    Arguments.of(null, "인자가 null인 경우"),
                    Arguments.of(new Object[]{}, "인자가 0개인 경우"),
                    Arguments.of(new Object[]{"arg1", "arg2"}, "인자가 설정된 개수(1개)보다 많은 경우")
            );
        }

        @Test
        void 인자_타입이_적절하지_않으면_예외가_발생한다() throws NoSuchFieldException, IllegalAccessException {
            // given
            StorageKey keyType = StorageKey.PICKEAT;
            java.lang.reflect.Field field = StorageKey.class.getDeclaredField("format");
            field.setAccessible(true);
            field.set(keyType, "test:%d");

            // when & then
            assertThatThrownBy(() -> keyType.generateKey("not-number"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(ErrorCode.INVALID_STORAGE_KEY_FORMAT.getMessage());
        }
    }
}
