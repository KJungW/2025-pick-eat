package com.pickeat.backend.pickeat.application;

import com.pickeat.backend.global.exception.code.ClientErrorCode;
import com.pickeat.backend.global.exception.type.ClientException;
import com.pickeat.backend.participant.domain.storage.ParticipantStorage;
import com.pickeat.backend.pickeat.application.dto.event.PickeatCompletionEventRequest;
import com.pickeat.backend.pickeat.application.dto.response.PickeatResultResponse;
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
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PickeatCompletionService {

    private final PickeatStorage pickeatStorage;
    private final RestaurantsStorage restaurantsStorage;
    private final ParticipantStorage participantStorage;
    private final PickeatRecordRepository pickeatRecordRepository;
    private final PickeatResultRepository pickeatResultRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void completePickeat(String pickeatCode) {
        Pickeat pickeat = getPickeatByCode(pickeatCode);
        Restaurant selectedRestaurant = selectResultRestaurant(pickeatCode);

        PickeatRecord pickeatRecord = savePickeatRecord(pickeat);
        PickeatResult pickeatResult = savePickeatResult(pickeatRecord, selectedRestaurant);

        removeAllAboutPickeatAtStorage(pickeatCode);
        publicPickeatPickeatCompletionEvent(pickeatCode, pickeatResult);
    }

    private Pickeat getPickeatByCode(String pickeatCode) {
        return pickeatStorage.getMeta(pickeatCode)
                .orElseThrow(() -> new ClientException(ClientErrorCode.PICKEAT_NOT_FOUND));
    }

    private Restaurants getRestaurantMetaInPickeat(String pickeatCode) {
        return restaurantsStorage.getRestaurantMeta(pickeatCode)
                .orElseThrow(() -> new ClientException(ClientErrorCode.RESTAURANT_NOT_FOUND));
    }

    private RestaurantStateDto getRestaurantStateInPickeat(String pickeatCode) {
        return restaurantsStorage.getRestaurantState(pickeatCode)
                .orElseThrow(() -> new ClientException(ClientErrorCode.RESTAURANT_NOT_FOUND));
    }

    private Restaurant selectResultRestaurant(String pickeatCode) {
        Restaurants restaurantMeta = getRestaurantMetaInPickeat(pickeatCode);
        RestaurantStateDto restaurantState = getRestaurantStateInPickeat(pickeatCode);
        return restaurantMeta.selectRestaurant(restaurantState);
    }

    private PickeatRecord savePickeatRecord(Pickeat pickeat) {
        PickeatRecord pickeatRecord = PickeatRecord.from(pickeat);
        return pickeatRecordRepository.save(pickeatRecord);
    }

    private PickeatResult savePickeatResult(PickeatRecord pickeatRecord, Restaurant selectedRestaurant) {
        PickeatResult pickeatResult = PickeatResult.from(pickeatRecord.getId(), selectedRestaurant);
        return pickeatResultRepository.save(pickeatResult);
    }

    private void removeAllAboutPickeatAtStorage(String pickeatCode) {
        pickeatStorage.remove(pickeatCode);
        restaurantsStorage.remove(pickeatCode);
        participantStorage.remove(pickeatCode);
    }

    private void publicPickeatPickeatCompletionEvent(String pickeatCode, PickeatResult pickeatResult) {
        PickeatCompletionEventRequest request = new PickeatCompletionEventRequest(
                pickeatCode,
                PickeatResultResponse.of(pickeatResult)
        );
        eventPublisher.publishEvent(request);
    }
}
