package com.pickeat.backend.pickeat.application;

import com.pickeat.backend.global.exception.ErrorCode;
import com.pickeat.backend.global.exception.type.ClientException;
import com.pickeat.backend.participant.domain.Participant;
import com.pickeat.backend.participant.domain.storage.ParticipantStorage;
import com.pickeat.backend.pickeat.application.dto.event.PickeatCompletionEventRequest;
import com.pickeat.backend.pickeat.application.dto.request.PickeatRequest;
import com.pickeat.backend.pickeat.application.dto.response.PickeatResponse;
import com.pickeat.backend.pickeat.application.dto.response.PickeatResultResponse;
import com.pickeat.backend.pickeat.application.dto.response.PickeatStateResponse;
import com.pickeat.backend.pickeat.domain.Pickeat;
import com.pickeat.backend.pickeat.domain.PickeatRecord;
import com.pickeat.backend.pickeat.domain.PickeatResult;
import com.pickeat.backend.pickeat.domain.repository.PickeatRecordRepository;
import com.pickeat.backend.pickeat.domain.repository.PickeatResultRepository;
import com.pickeat.backend.pickeat.domain.store.PickeatStorage;
import com.pickeat.backend.restaurant.application.dto.RestaurantStateDto;
import com.pickeat.backend.restaurant.domain.Restaurant;
import com.pickeat.backend.restaurant.domain.Restaurants;
import com.pickeat.backend.restaurant.domain.storage.RestaurantsStorage;
import com.pickeat.backend.room.domain.repository.RoomUserRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PickeatService {

    private final PickeatStorage pickeatStorage;
    private final RestaurantsStorage restaurantsStorage;
    private final ParticipantStorage participantStorage;
    private final RoomUserRepository roomUserRepository;
    private final PickeatRecordRepository pickeatRecordRepository;
    private final PickeatResultRepository pickeatResultRepository;
    private final ApplicationEventPublisher eventPublisher;

    public PickeatResponse createPickeatWithoutRoom(PickeatRequest request) {
        Pickeat pickeat = Pickeat.createWithoutRoom(request.name());
        pickeatStorage.save(pickeat);
        return PickeatResponse.from(pickeat);
    }

    public PickeatResponse createPickeatWithRoom(Long roomId, Long userId, PickeatRequest request) {
        validateUserAccessToRoom(roomId, userId);
        Pickeat pickeat = Pickeat.createWithRoom(request.name(), roomId);
        pickeatStorage.save(pickeat);
        return PickeatResponse.from(pickeat);
    }

    @Transactional
    public void completePickeat(String pickeatCode) {
        Pickeat pickeat = getPickeatByCode(pickeatCode);
        List<Participant> participants = getParticipantInPickeat(pickeatCode);
        Restaurant selectedRestaurant = selectRestaurantInPickeat(pickeatCode);

        PickeatRecord pickeatRecord = savePickeatRecord(pickeat);
        PickeatResult pickeatResult = savePickeatResult(pickeatRecord, selectedRestaurant);
        removeAllAboutPickeatAtStorage(pickeatCode);

        eventPublisher.publishEvent(
                new PickeatCompletionEventRequest(pickeatCode, PickeatResultResponse.of(pickeatResult)));
    }

    public PickeatResponse getPickeatMeta(String pickeatCode) {
        Optional<Pickeat> pickeat = pickeatStorage.get(pickeatCode);
        if (pickeat.isPresent()) {
            return PickeatResponse.from(pickeat.get());
        }
        Optional<PickeatRecord> pickeatRecord = pickeatRecordRepository.findByCode(pickeatCode);
        if (pickeatRecord.isPresent()) {
            return PickeatResponse.from(pickeatRecord.get());
        }
        throw new ClientException(ErrorCode.PICKEAT_NOT_FOUND);
    }

    public PickeatStateResponse getPickeatState(String pickeatCode) {
        Optional<Pickeat> pickeat = pickeatStorage.get(pickeatCode);
        if (pickeat.isPresent()) {
            return new PickeatStateResponse(false);
        }
        Optional<PickeatRecord> pickeatRecord = pickeatRecordRepository.findByCode(pickeatCode);
        if (pickeatRecord.isPresent()) {
            return new PickeatStateResponse(true);
        }
        throw new ClientException(ErrorCode.PICKEAT_NOT_FOUND);
    }

    private Pickeat getPickeatByCode(String pickeatCode) {
        return pickeatStorage.get(pickeatCode)
                .orElseThrow(() -> new ClientException(ErrorCode.PROCESSING_PICKEAT_NOT_FOUND));
    }

    private Restaurants getRestaurantMetaInPickeat(String pickeatCode) {
        return restaurantsStorage.getRestaurantMeta(pickeatCode)
                .orElseThrow(() -> new ClientException(ErrorCode.RESTAURANT_NOT_FOUND));
    }

    private RestaurantStateDto getRestaurantStateInPickeat(String pickeatCode) {
        return restaurantsStorage.getRestaurantState(pickeatCode)
                .orElseThrow(() -> new ClientException(ErrorCode.RESTAURANT_NOT_FOUND));
    }

    private List<Participant> getParticipantInPickeat(String pickeatCode) {
        return participantStorage.getParticipantsMeta(pickeatCode);
    }

    private PickeatRecord savePickeatRecord(Pickeat pickeat) {
        PickeatRecord pickeatRecord = PickeatRecord.from(pickeat);
        return pickeatRecordRepository.save(pickeatRecord);
    }

    private PickeatResult savePickeatResult(PickeatRecord pickeatRecord, Restaurant selectedRestaurant) {
        PickeatResult pickeatResult = PickeatResult.from(pickeatRecord.getId(), selectedRestaurant);
        return pickeatResultRepository.save(pickeatResult);
    }

    private void validateUserAccessToRoom(Long roomId, Long userId) {
        if (!roomUserRepository.existsByRoomIdAndUserId(roomId, userId)) {
            throw new ClientException(ErrorCode.ROOM_ACCESS_DENIED);
        }
    }

    private void removeAllAboutPickeatAtStorage(String pickeatCode) {
        pickeatStorage.remove(pickeatCode);
        restaurantsStorage.remove(pickeatCode);
        participantStorage.remove(pickeatCode);
    }

    private Restaurant selectRestaurantInPickeat(String pickeatCode) {
        Restaurants restaurantMeta = getRestaurantMetaInPickeat(pickeatCode);
        RestaurantStateDto restaurantState = getRestaurantStateInPickeat(pickeatCode);
        return restaurantMeta.selectRestaurant(restaurantState);
    }
}
