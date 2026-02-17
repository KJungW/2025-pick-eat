package com.pickeat.backend.pickeat.domain.repository;

import com.pickeat.backend.pickeat.domain.PickeatResultV2;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PickeatResultRepositoryV2 extends JpaRepository<PickeatResultV2, Long> {

    Optional<PickeatResultV2> findByPickeatRecordId(Long pickeatRecordId);
}
