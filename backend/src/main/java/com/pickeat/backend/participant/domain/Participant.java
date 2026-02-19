package com.pickeat.backend.participant.domain;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class Participant {

    private final String code;
    private final String nickname;

    public Participant(String nickname) {
        this.code = UUID.randomUUID().toString();
        this.nickname = nickname;
    }
}
