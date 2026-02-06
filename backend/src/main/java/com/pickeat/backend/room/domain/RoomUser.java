package com.pickeat.backend.room.domain;

import com.pickeat.backend.global.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE room_user SET deleted_at = NOW() WHERE id = ?")
public class RoomUser extends BaseEntity {

    @Column(nullable = false)
    private Long roomId;

    @Column(nullable = false)
    private Long userId;

    public RoomUser(Long roomId, Long userId) {
        this.roomId = roomId;
        this.userId = userId;
    }
}
