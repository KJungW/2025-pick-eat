package com.pickeat.backend.global.argument.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;

import com.pickeat.backend.global.argument.annotation.User;
import com.pickeat.backend.global.argument.principal.UserPrincipal;
import com.pickeat.backend.global.exception.ErrorCode;
import com.pickeat.backend.global.exception.type.BusinessException;
import com.pickeat.backend.login.application.UserTokenProvider;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;

@ExtendWith(MockitoExtension.class)
class UserArgumentResolverTest {

    @InjectMocks
    private UserArgumentResolver userArgumentResolver;

    @Mock
    private UserTokenProvider userTokenProvider;

    @Mock
    private MethodParameter methodParameter;

    @Mock
    private NativeWebRequest webRequest;

    private static final String VALID_TOKEN = "valid.user.token";
    private static final String AUTH_HEADER = "Bearer " + VALID_TOKEN;

    @Nested
    class 파라미터_지원_여부_확인 {

        @Test
        void 파라미터에_적용된_애노테이션을_확인한다() {
            // given
            given(methodParameter.hasParameterAnnotation(User.class)).willReturn(true);
            doReturn(UserPrincipal.class).when(methodParameter).getParameterType();

            // when
            boolean result = userArgumentResolver.supportsParameter(methodParameter);

            // then
            assertThat(result).isTrue();
        }
    }

    @Nested
    class 인자_Resolving {

        @Test
        void 인자를_적절히_리졸빙할_수_있다() {
            // given
            given(webRequest.getHeader("Authorization")).willReturn(AUTH_HEADER);
            given(userTokenProvider.getUserId(VALID_TOKEN)).willReturn(1L);

            // when
            Object result = userArgumentResolver.resolveArgument(methodParameter, null, webRequest, null);

            // then
            UserPrincipal principal = (UserPrincipal) result;
            assertAll(
                    () -> assertThat(result).isInstanceOf(UserPrincipal.class),
                    () -> assertThat(principal.userId()).isEqualTo(1L)
            );
        }

        @Test
        void 헤더가_비어있는_경우_예외처리() {
            // given
            given(webRequest.getHeader("Authorization")).willReturn(null);

            // when & then
            assertThatThrownBy(() -> userArgumentResolver.resolveArgument(methodParameter, null, webRequest, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(ErrorCode.INVALID_AUTH_HEADER.getMessage());
        }

        @Test
        void 헤더_prefix가_올바르지_않은_경우_예외처리() {
            // given
            given(webRequest.getHeader("Authorization")).willReturn("InvalidPrefix " + VALID_TOKEN);

            // when & then
            assertThatThrownBy(() -> userArgumentResolver.resolveArgument(methodParameter, null, webRequest, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(ErrorCode.INVALID_AUTH_HEADER.getMessage());
        }

        @Test
        void 토큰이_올바르지_않은_경우_예외처리() {
            // given
            given(webRequest.getHeader("Authorization")).willReturn(AUTH_HEADER);
            given(userTokenProvider.getUserId(VALID_TOKEN))
                    .willThrow(new BusinessException(ErrorCode.INVALID_TOKEN));

            // when & then
            assertThatThrownBy(() -> userArgumentResolver.resolveArgument(methodParameter, null, webRequest, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(ErrorCode.INVALID_TOKEN.getMessage());
        }
    }
}
