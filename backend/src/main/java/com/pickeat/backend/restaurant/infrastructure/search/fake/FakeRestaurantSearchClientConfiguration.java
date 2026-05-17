package com.pickeat.backend.restaurant.infrastructure.search.fake;

import static com.pickeat.backend.global.configuration.profile.EnvironmentProfile.PERFORMANCE;

import com.pickeat.backend.global.log.LogWriter;
import com.pickeat.backend.global.log.model.bussiness.DebugLog;
import com.pickeat.backend.restaurant.application.client.RestaurantSearchClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Slf4j
@Profile(PERFORMANCE)
@Configuration
@RequiredArgsConstructor
public class FakeRestaurantSearchClientConfiguration {

    @Bean
    public RestaurantSearchClient fakeRestaurantSearchClient() {
        LogWriter.warn(this.getClass(), DebugLog.of("FakeRestaurantSearchClient 활성화"));
        return new FakeRestaurantSearchClient();
    }
}
