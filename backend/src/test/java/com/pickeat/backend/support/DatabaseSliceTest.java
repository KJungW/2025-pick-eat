package com.pickeat.backend.support;

import com.pickeat.backend.support.utility.DatabaseCleaner;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;

@DataJpaTest
@Import({DatabaseCleaner.class})
public class DatabaseSliceTest {

    private static final MySQLContainer<?> MYSQL_CONTAINER;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @AfterEach
    void clear() {
        databaseCleaner.execute();
    }

    static {
        MYSQL_CONTAINER = new MySQLContainer<>("mysql:8.0")
                .withDatabaseName("picket_db")
                .withUsername("test")
                .withPassword("test")
                .withCommand("--log-bin-trust-function-creators=1");
        MYSQL_CONTAINER.start();
    }

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MYSQL_CONTAINER::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
    }
}
