package com.pickeat.backend.pickeat.application;

import com.pickeat.backend.global.exception.BusinessException;
import com.pickeat.backend.global.exception.ErrorCode;
import com.pickeat.backend.participant.domain.ParticipantV2;
import com.pickeat.backend.participant.domain.storage.ParticipantStorage;
import com.pickeat.backend.pickeat.application.dto.request.PickeatRequest;
import com.pickeat.backend.pickeat.application.dto.response.PickeatResponseV2;
import com.pickeat.backend.pickeat.application.dto.response.PickeatStateResponseV2;
import com.pickeat.backend.pickeat.domain.PickeatRecord;
import com.pickeat.backend.pickeat.domain.PickeatResultV2;
import com.pickeat.backend.pickeat.domain.PickeatV2;
import com.pickeat.backend.pickeat.domain.repository.PickeatRecordRepository;
import com.pickeat.backend.pickeat.domain.repository.PickeatResultRepositoryV2;
import com.pickeat.backend.pickeat.domain.store.PickeatStorage;
import com.pickeat.backend.restaurant.application.dto.RestaurantStateDto;
import com.pickeat.backend.restaurant.domain.RestaurantV2;
import com.pickeat.backend.restaurant.domain.RestaurantsV2;
import com.pickeat.backend.restaurant.domain.storage.RestaurantsStorage;
import com.pickeat.backend.room.domain.repository.RoomUserRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PickeatServiceV2 {

    private final PickeatStorage pickeatStorage;
    private final RestaurantsStorage restaurantsStorage;
    private final ParticipantStorage participantStorage;
    private final RoomUserRepository roomUserRepository;
    private final PickeatRecordRepository pickeatRecordRepository;
    private final PickeatResultRepositoryV2 pickeatResultRepository;

    public PickeatResponseV2 createPickeatWithoutRoom(PickeatRequest request) {
        PickeatV2 pickeat = PickeatV2.createWithoutRoom(request.name());
        pickeatStorage.save(pickeat);
        return PickeatResponseV2.from(pickeat);
    }

    public PickeatResponseV2 createPickeatWithRoom(Long roomId, Long userId, PickeatRequest request) {
        validateUserAccessToRoom(roomId, userId);
        PickeatV2 pickeat = PickeatV2.createWithRoom(request.name(), roomId);
        pickeatStorage.save(pickeat);
        return PickeatResponseV2.from(pickeat);
    }

    public void completePickeat(String pickeatCode) {
        PickeatV2 pickeat = getPickeatByCode(pickeatCode);
        List<ParticipantV2> participants = getParticipantInPickeat(pickeatCode);
        RestaurantV2 selectedRestaurant = selectRestaurantInPickeat(pickeatCode);

        PickeatRecord pickeatRecord = savePickeatRecord(pickeat);
        PickeatResultV2 pickeatResult = savePickeatResult(pickeatRecord, selectedRestaurant);

        removeAllAboutPickeatAtStorage(pickeatCode);
    }

    public PickeatResponseV2 getPickeatMeta(String pickeatCode) {
        Optional<PickeatV2> pickeat = pickeatStorage.get(pickeatCode);
        if (pickeat.isPresent()) {
            return PickeatResponseV2.from(pickeat.get());
        }
        Optional<PickeatRecord> pickeatRecord = pickeatRecordRepository.findByCode(pickeatCode);
        if (pickeatRecord.isPresent()) {
            return PickeatResponseV2.from(pickeatRecord.get());
        }
        throw new BusinessException(ErrorCode.PICKEAT_NOT_FOUND);
    }

    public PickeatStateResponseV2 getPickeatState(String pickeatCode) {
        Optional<PickeatV2> pickeat = pickeatStorage.get(pickeatCode);
        if (pickeat.isPresent()) {
            return new PickeatStateResponseV2(false);
        }
        Optional<PickeatRecord> pickeatRecord = pickeatRecordRepository.findByCode(pickeatCode);
        if (pickeatRecord.isPresent()) {
            return new PickeatStateResponseV2(true);
        }
        throw new BusinessException(ErrorCode.PICKEAT_NOT_FOUND);
    }

    private PickeatV2 getPickeatByCode(String pickeatCode) {
        return pickeatStorage.get(pickeatCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROCESSING_PICKEAT_NOT_FOUND));
    }

    private RestaurantsV2 getRestaurantMetaInPickeat(String pickeatCode) {
        return restaurantsStorage.getAllRestaurantMeta(pickeatCode)
                .orElse(new RestaurantsV2(List.of()));
    }

    private RestaurantStateDto getRestaurantStateInPickeat(String pickeatCode) {
        return restaurantsStorage.getAllRestaurantState(pickeatCode);
    }

    private List<ParticipantV2> getParticipantInPickeat(String pickeatCode) {
        return participantStorage.getParticipantsMeta(pickeatCode);
    }

    private PickeatRecord savePickeatRecord(PickeatV2 pickeat) {
        PickeatRecord pickeatRecord = PickeatRecord.from(pickeat);
        return pickeatRecordRepository.save(pickeatRecord);
    }

    private PickeatResultV2 savePickeatResult(PickeatRecord pickeatRecord, RestaurantV2 selectedRestaurant) {
        PickeatResultV2 pickeatResult = PickeatResultV2.from(pickeatRecord.getId(), selectedRestaurant);
        return pickeatResultRepository.save(pickeatResult);
    }

    private void validateUserAccessToRoom(Long roomId, Long userId) {
        if (!roomUserRepository.existsByRoomIdAndUserId(roomId, userId)) {
            throw new BusinessException(ErrorCode.ROOM_ACCESS_DENIED);
        }
    }

    private void removeAllAboutPickeatAtStorage(String pickeatCode) {
        pickeatStorage.remove(pickeatCode);
        restaurantsStorage.remove(pickeatCode);
        participantStorage.remove(pickeatCode);
    }

    private RestaurantV2 selectRestaurantInPickeat(String pickeatCode) {
        RestaurantsV2 restaurantMeta = getRestaurantMetaInPickeat(pickeatCode);
        RestaurantStateDto restaurantState = getRestaurantStateInPickeat(pickeatCode);
        return restaurantMeta.selectRestaurant(restaurantState);
    }
}
