package com.pickeat.backend.global.cache;

import com.pickeat.backend.template.application.listener.TemplateCacheEventListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class CacheEventListenerConfiguration {

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            TemplateCacheEventListener templateCacheEventListener
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(templateCacheEventListener,
                new ChannelTopic(CacheChannelTopic.TEMPLATE_TOPIC.getValue()));
        return container;
    }
}
