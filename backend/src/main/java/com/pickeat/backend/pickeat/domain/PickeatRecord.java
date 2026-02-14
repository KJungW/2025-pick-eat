package com.pickeat.backend.pickeat.domain;

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
@SQLDelete(sql = "UPDATE pickeat_record SET deleted_at = NOW() WHERE id = ?")
public class PickeatRecord extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    private Long roomId;

    public PickeatRecord(String code, String name, Long roomId) {
        this.code = code;
        this.name = name;
        this.roomId = roomId;
    }

    public static PickeatRecord from(PickeatV2 pickeat) {
        if (pickeat.belongsToRoom()) {
            return new PickeatRecord(pickeat.getCode(), pickeat.getName(), pickeat.getRoomId());
        }
        return new PickeatRecord(pickeat.getCode(), pickeat.getName(), null);
    }
}
