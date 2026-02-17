package com.pickeat.backend.pickeat.domain.repository;

import com.pickeat.backend.pickeat.domain.PickeatRecord;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PickeatRecordRepository extends JpaRepository<PickeatRecord, Long> {

    Optional<PickeatRecord> findByCode(String code);
}
