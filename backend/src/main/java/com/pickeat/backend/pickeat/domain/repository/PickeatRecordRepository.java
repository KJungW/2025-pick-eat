package com.pickeat.backend.pickeat.domain.repository;

import com.pickeat.backend.pickeat.domain.PickeatRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PickeatRecordRepository extends JpaRepository<PickeatRecord, Long> {

}
