package com.pickeat.backend.support;

import com.pickeat.backend.global.cache.CacheConfiguration;
import com.pickeat.backend.global.config.VirtualThreadExecutorConfig;
import com.pickeat.backend.global.limiter.ExternalApiTrafficLimiterConfiguration;
import com.pickeat.backend.global.utility.JsonParser;
import com.pickeat.backend.support.utility.CacheCleaner;
import com.pickeat.backend.support.utility.DatabaseCleaner;
import com.pickeat.backend.support.utility.StorageCleaner;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

@DataJpaTest
@Import({
        DatabaseCleaner.class,
        StorageCleaner.class,
        CacheCleaner.class,
        RedisAutoConfiguration.class,
        CacheConfiguration.class,
        VirtualThreadExecutorConfig.class,
        ExternalApiTrafficLimiterConfiguration.class,
        JsonParser.class})
public class DatabaseSliceTest {

    private static final MySQLContainer<?> MYSQL_CONTAINER;
    private static final GenericContainer<?> REDIS_CONTAINER;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private StorageCleaner storageCleaner;

    @Autowired
    private CacheCleaner cacheCleaner;

    @AfterEach
    void clear() {
        databaseCleaner.execute();
        storageCleaner.execute();
        cacheCleaner.execute();
    }

    static {
        MYSQL_CONTAINER = new MySQLContainer<>("mysql:8.0")
                .withDatabaseName("picket_db")
                .withUsername("test")
                .withPassword("test")
                .withCommand("--log-bin-trust-function-creators=1");
        MYSQL_CONTAINER.start();

        REDIS_CONTAINER = new GenericContainer<>(DockerImageName.parse("redis:latest"))
                .withExposedPorts(6379)
                .withCommand("redis-server --requirepass pickeat123");
        REDIS_CONTAINER.start();
    }

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MYSQL_CONTAINER::getPassword);
        registry.add("spring.datasource.hikari.maximum-pool-size", () -> 3);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");

        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port", () -> REDIS_CONTAINER.getMappedPort(6379));
        registry.add("spring.data.redis.password", () -> "pickeat123");
    }
}
