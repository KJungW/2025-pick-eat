package com.pickeat.backend.global.limiter;

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.expiration.BasedOnTimeForRefillingBucketUpToMaxExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.Bucket4jLettuce;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExternalApiTrafficLimiterConfiguration {

    private static final Duration DELETE_BUCKET_TIME = Duration.ofMinutes(10);

    @Bean
    public ProxyManager<String> bucket4jProxyManager(
            @Value("${spring.data.redis.host}") String host,
            @Value("${spring.data.redis.port}") int port,
            @Value("${spring.data.redis.password}") String password,
            @Value("${spring.data.redis.timeout}") Duration timeout
    ) {
        RedisClient redisClient = RedisClient.create(
                RedisURI.builder()
                        .withHost(host)
                        .withPort(port)
                        .withPassword(password.toCharArray())
                        .withTimeout(timeout)
                        .build()
        );

        StatefulRedisConnection<String, byte[]> connection =
                redisClient.connect(RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE));

        ExpirationAfterWriteStrategy expirationStrategy =
                new BasedOnTimeForRefillingBucketUpToMaxExpirationAfterWriteStrategy(DELETE_BUCKET_TIME);

        return Bucket4jLettuce.casBasedBuilder(connection)
                .expirationAfterWrite(expirationStrategy)
                .build();
    }
}
