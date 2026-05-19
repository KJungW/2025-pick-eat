package com.pickeat.backend.room.application.dto.response;

import com.pickeat.backend.room.application.dto.repository.RoomWithUserCountDto;
import com.pickeat.backend.room.domain.Room;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "방 정보 응답", name = "RoomResponseV2")
public record RoomResponse(
        @Schema(description = "방 ID", example = "1")
        long id,
        @Schema(description = "방 이름", example = "점심 같이 먹어요")
        String name,
        @Schema(description = "방에 참여한 인원 수", example = "3")
        int userCount
) {

    public static RoomResponse from(RoomWithUserCountDto dto) {
        Room room = dto.room();
        int userCount = dto.userCount();
        return new RoomResponse(room.getId(), room.getName(), userCount);
    }

    public static List<RoomResponse> from(List<RoomWithUserCountDto> dto) {
        return dto.stream()
                .map(RoomResponse::from)
                .toList();
    }

    public static RoomResponse of(Room room, int userCount) {
        return new RoomResponse(room.getId(), room.getName(), userCount);
    }
}
