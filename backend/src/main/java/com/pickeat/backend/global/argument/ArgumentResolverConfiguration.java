package com.pickeat.backend.global.argument;

import com.pickeat.backend.global.argument.resolver.OAuthProviderArgumentResolver;
import com.pickeat.backend.global.argument.resolver.ParticipantArgumentResolver;
import com.pickeat.backend.global.argument.resolver.UserArgumentResolver;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class ArgumentResolverConfiguration implements WebMvcConfigurer {

    private final UserArgumentResolver userArgumentResolver;
    private final ParticipantArgumentResolver participantArgumentResolver;
    private final OAuthProviderArgumentResolver oauthProviderArgumentResolver;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(userArgumentResolver);
        resolvers.add(participantArgumentResolver);
        resolvers.add(oauthProviderArgumentResolver);
    }
}
