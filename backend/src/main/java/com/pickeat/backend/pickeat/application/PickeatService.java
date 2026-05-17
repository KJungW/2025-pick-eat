package com.pickeat.backend.pickeat.application;

import com.pickeat.backend.global.exception.code.ClientErrorCode;
import com.pickeat.backend.global.exception.type.ClientException;
import com.pickeat.backend.pickeat.application.dto.request.PickeatRequest;
import com.pickeat.backend.pickeat.application.dto.response.PickeatResponse;
import com.pickeat.backend.pickeat.application.dto.response.PickeatStateResponse;
import com.pickeat.backend.pickeat.domain.Pickeat;
import com.pickeat.backend.pickeat.domain.PickeatRecord;
import com.pickeat.backend.pickeat.domain.repository.PickeatRecordRepository;
import com.pickeat.backend.pickeat.domain.store.PickeatStorage;
import com.pickeat.backend.room.domain.repository.RoomUserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PickeatService {

    private final PickeatStorage pickeatStorage;
    private final RoomUserRepository roomUserRepository;
    private final PickeatRecordRepository pickeatRecordRepository;

    public PickeatResponse createPickeatWithoutRoom(PickeatRequest request) {
        Pickeat pickeat = Pickeat.createWithoutRoom(request.name());
        pickeatStorage.save(pickeat);
        return PickeatResponse.from(pickeat);
    }

    public PickeatResponse createPickeatWithRoom(Long roomId, Long userId, PickeatRequest request) {
        canAccessRoom(roomId, userId);
        Pickeat pickeat = Pickeat.createWithRoom(request.name(), roomId);
        pickeatStorage.save(pickeat);
        return PickeatResponse.from(pickeat);
    }

    public PickeatResponse getPickeatMeta(String pickeatCode) {
        // 픽잇이 진행중일 경우에는 Storage 에서 조회
        Optional<Pickeat> pickeat = pickeatStorage.getMeta(pickeatCode);
        if (pickeat.isPresent()) {
            return PickeatResponse.from(pickeat.get());
        }
        // 픽잇이 종료된 경우에는 Repository 에서 조회
        Optional<PickeatRecord> pickeatRecord = pickeatRecordRepository.findByCode(pickeatCode);
        if (pickeatRecord.isPresent()) {
            return PickeatResponse.from(pickeatRecord.get());
        }
        throw new ClientException(ClientErrorCode.PICKEAT_NOT_FOUND);
    }

    public PickeatStateResponse getPickeatState(String pickeatCode) {
        // Storage 에서 픽잇이 조회된 경우, 해당 픽잇은 진행 중
        Optional<Pickeat> pickeat = pickeatStorage.getMeta(pickeatCode);
        if (pickeat.isPresent()) {
            return new PickeatStateResponse(false);
        }
        // Repository 에서 픽잇이 조회딘 경우, 해당 픽잇은 종료
        Optional<PickeatRecord> pickeatRecord = pickeatRecordRepository.findByCode(pickeatCode);
        if (pickeatRecord.isPresent()) {
            return new PickeatStateResponse(true);
        }
        throw new ClientException(ClientErrorCode.PICKEAT_NOT_FOUND);
    }

    private void canAccessRoom(Long roomId, Long userId) {
        if (!roomUserRepository.existsByRoomIdAndUserId(roomId, userId)) {
            throw new ClientException(ClientErrorCode.ROOM_ACCESS_DENIED);
        }
    }
}
