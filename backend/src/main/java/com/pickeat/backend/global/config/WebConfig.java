package com.pickeat.backend.global.config;

import com.pickeat.backend.global.argument.resolver.OAuthProviderArgumentResolver;
import com.pickeat.backend.global.argument.resolver.ParticipantArgumentResolver;
import com.pickeat.backend.global.argument.resolver.UserArgumentResolver;
import com.pickeat.backend.global.version.DeprecationInterceptor;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final UserArgumentResolver userArgumentResolver;
    private final ParticipantArgumentResolver participantArgumentResolver;
    private final OAuthProviderArgumentResolver OAuthProviderArgumentResolver;
    private final DeprecationInterceptor deprecationInterceptor;

    @Value("${cors.allowed-origins}")
    private String[] allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(userArgumentResolver);
        resolvers.add(participantArgumentResolver);
        resolvers.add(OAuthProviderArgumentResolver);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(deprecationInterceptor)
                .addPathPatterns("/api/v1/**");
    }
}
