package com.pickeat.backend.participant.application;

import com.pickeat.backend.global.exception.code.ClientErrorCode;
import com.pickeat.backend.global.exception.type.ClientException;
import com.pickeat.backend.login.application.dto.response.TokenResponse;
import com.pickeat.backend.participant.application.dto.ParticipantStateDto;
import com.pickeat.backend.participant.application.dto.event.ParticipantUpdateEventRequest;
import com.pickeat.backend.participant.application.dto.request.ParticipantRequest;
import com.pickeat.backend.participant.application.dto.response.ParticipantResponse;
import com.pickeat.backend.participant.application.dto.response.ParticipantStateResponse;
import com.pickeat.backend.participant.domain.Participant;
import com.pickeat.backend.participant.domain.storage.ParticipantStorage;
import com.pickeat.backend.pickeat.domain.Pickeat;
import com.pickeat.backend.pickeat.domain.store.PickeatStorage;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipantService {

    private final PickeatStorage pickeatStorage;
    private final ParticipantStorage participantStorage;
    private final ParticipantTokenProvider participantTokenProvider;
    private final ApplicationEventPublisher eventPublisher;

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

    private void setupAboutParticipant(Pickeat pickeat, Participant participant) {
        boolean isSuccess = participantStorage.saveAboutParticipant(pickeat.getCode(), participant);
        if (!isSuccess) {
            throw new ClientException(ClientErrorCode.PARTICIPANT_ALREADY_EXISTS);
        }
    }

    private Pickeat getPickeatByCode(String pickeatCode) {
        return pickeatStorage.getMeta(pickeatCode)
                .orElseThrow(() -> new ClientException(ClientErrorCode.PICKEAT_NOT_FOUND));
    }

    private List<Participant> getParticipantsMetaInPickeat(String pickeatCode) {
        return participantStorage.getParticipantsMeta(pickeatCode);
    }

    private ParticipantStateDto getParticipantsStateInPickeat(String pickeatCode) {
        return participantStorage.getParticipantsState(pickeatCode)
                .orElseThrow(() -> new ClientException(ClientErrorCode.PARTICIPANT_NOT_FOUND));
    }

    private void publishParticipantUpdateEvent(String pickeatCode) {
        ParticipantStateDto state = getParticipantsStateInPickeat(pickeatCode);
        ParticipantUpdateEventRequest EventRequest = ParticipantUpdateEventRequest.of(state, pickeatCode);
        eventPublisher.publishEvent(EventRequest);
    }
}
