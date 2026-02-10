package com.pickeat.backend.pickeat.application;

import com.pickeat.backend.global.exception.BusinessException;
import com.pickeat.backend.global.exception.ErrorCode;
import com.pickeat.backend.login.application.dto.response.TokenResponse;
import com.pickeat.backend.pickeat.application.dto.request.ParticipantRequestV2;
import com.pickeat.backend.pickeat.application.dto.response.ParticipantResponseV2;
import com.pickeat.backend.pickeat.domain.ParticipantV2;
import com.pickeat.backend.pickeat.domain.PickeatV2;
import com.pickeat.backend.pickeat.domain.store.ParticipantStorage;
import com.pickeat.backend.pickeat.domain.store.PickeatStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipantServiceV2 {

    private final PickeatStorage pickeatStorage;
    private final ParticipantStorage participantStorage;
    private final ParticipantTokenProviderV2 participantTokenProvider;

    public TokenResponse createParticipant(ParticipantRequestV2 request) {
        PickeatV2 pickeat = getPickeatByCode(request.pickeatCode());
        ParticipantV2 participant = new ParticipantV2(request.nickname());
        participantStorage.save(pickeat.getCode(), participant);
        return participantTokenProvider.createToken(participant, pickeat);
    }

    public ParticipantResponseV2 getParticipant(String pickeatCode, String participantCode) {
        PickeatV2 pickeat = getPickeatByCode(pickeatCode);
        ParticipantV2 participant = getParticipantByCode(pickeatCode, participantCode);
        return ParticipantResponseV2.from(participant);
    }

    private PickeatV2 getPickeatByCode(String pickeatCode) {
        return pickeatStorage.get(pickeatCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.PICKEAT_NOT_FOUND));
    }

    private ParticipantV2 getParticipantByCode(String pickeatCode, String participantCode) {
        return participantStorage.get(pickeatCode, participantCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARTICIPANT_NOT_FOUND));
    }
}
