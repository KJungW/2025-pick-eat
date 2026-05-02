package com.pickeat.backend.global.setting;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StorageMigrationHistoryRepository extends JpaRepository<StorageMigrationHistory, Long> {

    boolean existsByState(StorageMigrationState state);

    boolean existsByVersionAndState(Long version, StorageMigrationState state);
}
