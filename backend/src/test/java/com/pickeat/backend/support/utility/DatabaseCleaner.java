package com.pickeat.backend.support.utility;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Table;
import jakarta.persistence.metamodel.EntityType;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("test")
public class DatabaseCleaner {

    @PersistenceContext
    private EntityManager entityManager;

    private List<String> tableNames;

    @PostConstruct
    public void findTableNames() {
        tableNames = entityManager.getMetamodel().getEntities().stream()
                .map(this::findTableName)
                .filter(name -> !name.equals("flyway_schema_history"))
                .toList();
    }

    @Transactional
    public void execute() {
        entityManager.flush();
        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
        for (String tableName : tableNames) {
            entityManager.createNativeQuery("TRUNCATE TABLE " + tableName).executeUpdate();
        }
        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
    }

    private String findTableName(EntityType<?> entityType) {
        Table tableAnnotation = entityType.getJavaType().getAnnotation(Table.class);
        if (tableAnnotation != null && !tableAnnotation.name().isBlank()) {
            return tableAnnotation.name();
        }
        return entityType.getName().replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }
}
