package com.pickeat.backend.participant.domain.repository;

import com.pickeat.backend.participant.domain.Participant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {

    List<Participant> findByPickeatIdIn(List<Long> pickeatIds);

    List<Participant> findByPickeatId(Long pickeatId);
}
