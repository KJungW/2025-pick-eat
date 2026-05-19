package com.pickeat.backend.room.application;

import com.pickeat.backend.global.exception.code.ClientErrorCode;
import com.pickeat.backend.global.exception.type.ClientException;
import com.pickeat.backend.room.application.dto.repository.RoomWithUserCountDto;
import com.pickeat.backend.room.application.dto.request.RoomInvitationRequest;
import com.pickeat.backend.room.application.dto.request.RoomRequest;
import com.pickeat.backend.room.application.dto.response.RoomResponse;
import com.pickeat.backend.room.domain.Room;
import com.pickeat.backend.room.domain.RoomUser;
import com.pickeat.backend.room.domain.repository.RoomRepository;
import com.pickeat.backend.room.domain.repository.RoomUserBulkRepository;
import com.pickeat.backend.room.domain.repository.RoomUserRepository;
import com.pickeat.backend.user.domain.User;
import com.pickeat.backend.user.domain.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomService {

    private static final int INITIAL_ROOM_COUNT = 1;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final RoomUserRepository roomUserRepository;
    private final RoomUserBulkRepository roomUserBulkRepository;

    @Transactional
    public RoomResponse createRoom(RoomRequest request, Long userId) {
        Room room = new Room(request.name());
        roomRepository.save(room);
        roomUserRepository.save(new RoomUser(room.getId(), userId));
        return RoomResponse.of(room, INITIAL_ROOM_COUNT);
    }

    public RoomResponse getRoom(Long roomId, Long userId) {
        canAccessRoom(roomId, userId);
        RoomWithUserCountDto room = roomRepository.findWithUserCount(roomId);
        return RoomResponse.from(room);
    }

    public List<RoomResponse> getAllRoom(Long userId) {
        List<RoomWithUserCountDto> rooms = roomRepository.findWithUserCountByUser(userId);
        return RoomResponse.from(rooms);
    }

    @Transactional
    public void inviteUsers(Long roomId, Long userId, RoomInvitationRequest request) {
        canAccessRoom(roomId, userId);
        List<User> inviteTargets = findUserById(request.userIds());
        List<RoomUser> roomUsers = RoomUser.of(roomId, inviteTargets);
        roomUserBulkRepository.insertAll(roomUsers);
    }

    @Transactional
    public void exitRoom(Long roomId, Long userId) {
        roomUserRepository.deleteByRoomIdAndUserId(roomId, userId);
    }

    private void canAccessRoom(Long roomId, Long userId) {
        if (!roomUserRepository.existsByRoomIdAndUserId(roomId, userId)) {
            throw new ClientException(ClientErrorCode.ROOM_ACCESS_DENIED);
        }
    }

    private List<User> findUserById(List<Long> userIds) {
        return userRepository.findAllByIdIn(userIds);
    }
}
