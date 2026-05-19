package com.pickeat.backend.room.application.dto.repository;

import com.pickeat.backend.room.domain.Room;

public record RoomWithUserCountDto(
        Room room,
        int userCount
) {

}
