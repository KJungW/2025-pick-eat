package com.pickeat.backend.global.setting;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StorageMigrationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private Long version;
    private String scriptName;
    private LocalDateTime executedAt;
    private StorageMigrationState state;

    public StorageMigrationHistory(
            Long version,
            String scriptName
    ) {
        this.version = version;
        this.scriptName = scriptName;
        this.executedAt = LocalDateTime.now();
        this.state = StorageMigrationState.START;
    }

    public void completeMigration() {
        this.state = StorageMigrationState.COMPLETE;
    }

    public void failMigration() {
        this.state = StorageMigrationState.FAIL;
    }
}
