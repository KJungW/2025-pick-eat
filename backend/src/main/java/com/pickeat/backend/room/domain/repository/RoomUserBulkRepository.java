package com.pickeat.backend.room.domain.repository;

import com.pickeat.backend.room.domain.RoomUser;
import java.sql.PreparedStatement;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RoomUserBulkRepository {

    private final JdbcTemplate jdbcTemplate;

    public void insertAll(List<RoomUser> roomUsers) {
        String sql = "INSERT IGNORE INTO room_user (room_id, user_id) VALUES (?, ?)";
        jdbcTemplate.batchUpdate(
                sql, roomUsers, 20,
                (PreparedStatement ps, RoomUser roomUser) -> {
                    ps.setLong(1, roomUser.getRoomId());
                    ps.setLong(2, roomUser.getUserId());
                });
    }
}
