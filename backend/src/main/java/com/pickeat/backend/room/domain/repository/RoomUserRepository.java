package com.pickeat.backend.room.domain.repository;

import com.pickeat.backend.room.domain.RoomUser;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomUserRepository extends JpaRepository<RoomUser, Long> {

    List<RoomUser> findAllByRoomId(Long roomId);

    boolean existsByRoomIdAndUserId(Long roomId, Long userId);

    void deleteByRoomIdAndUserId(Long roomId, Long userId);
}
