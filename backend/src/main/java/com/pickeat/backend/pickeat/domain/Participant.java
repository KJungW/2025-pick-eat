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
@SQLDelete(sql = "UPDATE participant SET deleted_at = NOW() WHERE id = ?")
public class Participant extends BaseEntity {

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private Boolean isCompleted = false;

    @Column(nullable = false)
    private Long pickeatId;

    public Participant(String nickname, Long pickeatId) {
        this.nickname = nickname;
        this.pickeatId = pickeatId;
    }

    public void updateCompletionAs(boolean isCompleted) {
        this.isCompleted = isCompleted;
    }
}
