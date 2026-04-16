package com.pickeat.backend.participant.application;

import com.pickeat.backend.global.exception.BusinessException;
import com.pickeat.backend.global.exception.ErrorCode;
import com.pickeat.backend.global.utility.TransactionUtility;
import com.pickeat.backend.login.application.dto.response.TokenResponse;
import com.pickeat.backend.participant.application.dto.ParticipantStateDto;
import com.pickeat.backend.participant.application.dto.event.ParticipantUpdateEvent;
import com.pickeat.backend.participant.application.dto.request.ParticipantRequest;
import com.pickeat.backend.participant.application.dto.response.ParticipantResponse;
import com.pickeat.backend.participant.application.dto.response.ParticipantStateResponse;
import com.pickeat.backend.participant.application.publisher.ParticipantEventPublisher;
import com.pickeat.backend.participant.domain.Participant;
import com.pickeat.backend.participant.domain.storage.ParticipantStorage;
import com.pickeat.backend.pickeat.domain.Pickeat;
import com.pickeat.backend.pickeat.domain.store.PickeatStorage;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipantService {

    private final PickeatStorage pickeatStorage;
    private final ParticipantStorage participantStorage;
    private final ParticipantTokenProvider participantTokenProvider;
    private final ParticipantEventPublisher participantEventPublisher;

    public TokenResponse createParticipant(ParticipantRequest request) {
        Pickeat pickeat = getPickeatByCode(request.pickeatCode());
        Participant participant = new Participant(request.nickname());
        setupAboutParticipant(pickeat, participant);
        publishParticipantUpdateEvent(request.pickeatCode());
        return participantTokenProvider.createToken(participant, pickeat);
    }

    public List<ParticipantResponse> getMetaInPickeat(String pickeatCode) {
        Pickeat pickeat = getPickeatByCode(pickeatCode);
        List<Participant> participants = getParticipantsMetaInPickeat(pickeatCode);
        return ParticipantResponse.from(participants);
    }

    public ParticipantStateResponse getStateInPickeat(String pickeatCode) {
        Pickeat pickeat = getPickeatByCode(pickeatCode);
        ParticipantStateDto state = getParticipantsStateInPickeat(pickeatCode);
        return ParticipantStateResponse.from(state);
    }

    public void markCompletion(String pickeatCode, String participantCode) {
        Pickeat pickeat = getPickeatByCode(pickeatCode);
        participantStorage.markCompletion(pickeatCode, participantCode);
        publishParticipantUpdateEvent(pickeatCode);
    }

    public void cancelCompletion(String pickeatCode, String participantCode) {
        Pickeat pickeat = getPickeatByCode(pickeatCode);
        participantStorage.cancelCompletion(pickeatCode, participantCode);
        publishParticipantUpdateEvent(pickeatCode);
    }

    private Pickeat getPickeatByCode(String pickeatCode) {
        return pickeatStorage.get(pickeatCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROCESSING_PICKEAT_NOT_FOUND));
    }

    private List<Participant> getParticipantsMetaInPickeat(String pickeatCode) {
        return participantStorage.getParticipantsMeta(pickeatCode);
    }

    private ParticipantStateDto getParticipantsStateInPickeat(String pickeatCode) {
        return participantStorage.getParticipantsState(pickeatCode)
                .orElse(new ParticipantStateDto(Map.of()));
    }

    private void setupAboutParticipant(Pickeat pickeat, Participant participant) {
        boolean isSuccess = participantStorage.setupAboutParticipant(pickeat.getCode(), participant);
        if (!isSuccess) {
            throw new BusinessException(ErrorCode.PARTICIPANT_ALREADY_EXISTS);
        }
    }

    private void publishParticipantUpdateEvent(String pickeatCode) {
        TransactionUtility.doAfterCommit(() -> {
            ParticipantStateDto participantState = getParticipantsStateInPickeat(pickeatCode);
            ParticipantUpdateEvent event = new ParticipantUpdateEvent(pickeatCode, participantState.completionState());
            participantEventPublisher.publishParticipantUpdateEvent(event);
        });
    }
}
