package com.pickeat.backend.pickeat.domain;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class ParticipantV2 {

    private final String code;
    private final String nickname;

    public ParticipantV2(String nickname) {
        this.code = UUID.randomUUID().toString();
        this.nickname = nickname;
    }
}
