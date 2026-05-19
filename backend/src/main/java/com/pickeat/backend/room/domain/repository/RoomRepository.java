package com.pickeat.backend.room.domain.repository;

import com.pickeat.backend.room.application.dto.repository.RoomWithUserCountDto;
import com.pickeat.backend.room.domain.Room;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    @Query("""
                SELECT new com.example.dto.RoomWithCountDto(r, COUNT(ru))
                FROM Room r
                LEFT JOIN RoomUser ru ON ru.room = r
                WHERE r.id = :roomId
                GROUP BY r.id
            """)
    RoomWithUserCountDto findWithUserCount(@Param("roomId") Long roomId);

    @Query("""
                SELECT new com.example.dto.RoomWithCountDto(r, COUNT(ru))
                FROM RoomUser ru
                INNER JOIN ru.room r
                WHERE ru.user.id = :userId
                GROUP BY r.id
            """)
    List<RoomWithUserCountDto> findWithUserCountByUser(@Param("userId") Long userId);
}
