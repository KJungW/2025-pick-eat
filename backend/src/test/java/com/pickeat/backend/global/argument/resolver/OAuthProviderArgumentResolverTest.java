package com.pickeat.backend.global.argument.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;

import com.pickeat.backend.global.argument.annotation.OAuthProvider;
import com.pickeat.backend.global.argument.principal.OAuthProviderPrincipal;
import com.pickeat.backend.global.exception.ErrorCode;
import com.pickeat.backend.global.exception.type.BusinessException;
import com.pickeat.backend.login.application.OAuthProviderTokenProvider;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;

@ExtendWith(MockitoExtension.class)
class OAuthProviderArgumentResolverTest {

    @InjectMocks
    private OAuthProviderArgumentResolver oAuthProviderArgumentResolver;

    @Mock
    private OAuthProviderTokenProvider providerTokenProvider;

    @Mock
    private MethodParameter methodParameter;

    @Mock
    private NativeWebRequest webRequest;

    private static final String VALID_TOKEN = "valid.token.value";
    private static final String AUTH_HEADER = "Bearer " + VALID_TOKEN;

    @Nested
    class 파라미터_지원_여부_확인 {

        @Test
        void 파라미터에_적용된_애노테이션을_확인한다() {
            // given
            given(methodParameter.hasParameterAnnotation(OAuthProvider.class)).willReturn(true);
            doReturn(OAuthProviderPrincipal.class).when(methodParameter).getParameterType();

            // when
            boolean result = oAuthProviderArgumentResolver.supportsParameter(methodParameter);

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
            given(providerTokenProvider.getProviderId(VALID_TOKEN)).willReturn(1L);
            given(providerTokenProvider.getProvider(VALID_TOKEN)).willReturn("kakao");

            // when
            Object result = oAuthProviderArgumentResolver.resolveArgument(methodParameter, null, webRequest, null);

            // then
            OAuthProviderPrincipal principal = (OAuthProviderPrincipal) result;
            assertAll(
                    () -> assertThat(result).isInstanceOf(OAuthProviderPrincipal.class),
                    () -> assertThat(principal.providerId()).isEqualTo(1L),
                    () -> assertThat(principal.provider()).isEqualTo("kakao")
            );
        }

        @Test
        void 헤더가_비어있는_경우_예외처리() {
            // given
            given(webRequest.getHeader("Authorization")).willReturn(null);

            // when & then
            assertThatThrownBy(
                    () -> oAuthProviderArgumentResolver.resolveArgument(methodParameter, null, webRequest, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(ErrorCode.INVALID_AUTH_HEADER.getMessage());
        }

        @Test
        void 헤더_prefix가_올바르지_않은_경우_예외처리() {
            // given
            given(webRequest.getHeader("Authorization")).willReturn("Basic invalid_prefix");

            // when & then
            assertThatThrownBy(
                    () -> oAuthProviderArgumentResolver.resolveArgument(methodParameter, null, webRequest, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(ErrorCode.INVALID_AUTH_HEADER.getMessage());
        }

        @Test
        void 토큰이_올바르지_않은_경우_예외처리() {
            // given
            given(webRequest.getHeader("Authorization")).willReturn(AUTH_HEADER);
            given(providerTokenProvider.getProviderId(VALID_TOKEN))
                    .willThrow(new BusinessException(ErrorCode.INVALID_TOKEN));

            // when & then
            assertThatThrownBy(
                    () -> oAuthProviderArgumentResolver.resolveArgument(methodParameter, null, webRequest, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(ErrorCode.INVALID_TOKEN.getMessage());
        }
    }
}
