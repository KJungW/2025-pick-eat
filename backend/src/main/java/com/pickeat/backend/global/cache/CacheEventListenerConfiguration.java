package com.pickeat.backend.global.cache;

import com.pickeat.backend.template.application.listener.TemplateCacheEventListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
public class CacheEventListenerConfiguration {

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            TemplateCacheEventListener templateCacheEventListener
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        MessageListenerAdapter listAdapter = new MessageListenerAdapter(
                templateCacheEventListener, "processTemplateCacheInvalidationEvent");
        container.addMessageListener(listAdapter, new ChannelTopic("template-topic"));

        return container;
    }
}
