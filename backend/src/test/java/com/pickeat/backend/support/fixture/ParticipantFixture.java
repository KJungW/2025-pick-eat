package com.pickeat.backend.support.fixture;

import com.pickeat.backend.participant.domain.Participant;

public class ParticipantFixture {

    public static Participant create(Long pickeatId) {
        return new Participant("참가자", pickeatId);
    }
}
