package com.pickeat.backend.participant.application;

import com.pickeat.backend.global.exception.BusinessException;
import com.pickeat.backend.global.exception.ErrorCode;
import com.pickeat.backend.login.application.dto.response.TokenResponse;
import com.pickeat.backend.participant.application.dto.request.ParticipantRequestV2;
import com.pickeat.backend.participant.application.dto.response.ParticipantResponseV2;
import com.pickeat.backend.participant.domain.ParticipantV2;
import com.pickeat.backend.participant.domain.storage.ParticipantStorage;
import com.pickeat.backend.pickeat.domain.PickeatV2;
import com.pickeat.backend.pickeat.domain.store.PickeatStorage;
import java.util.List;
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
        participantStorage.setupAboutParticipant(pickeat.getCode(), participant);
        return participantTokenProvider.createToken(participant, pickeat);
    }

    public List<ParticipantResponseV2> getMetaInPickeat(String pickeatCode) {
        PickeatV2 pickeat = getPickeatByCode(pickeatCode);
        List<ParticipantV2> participants = participantStorage.getParticipants(pickeatCode);
        return ParticipantResponseV2.from(participants);
    }

    private PickeatV2 getPickeatByCode(String pickeatCode) {
        return pickeatStorage.get(pickeatCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.PICKEAT_NOT_FOUND));
    }
}
