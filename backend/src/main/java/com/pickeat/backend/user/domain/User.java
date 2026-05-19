package com.pickeat.backend.user.domain;

import com.pickeat.backend.global.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE users SET deleted_at = NOW() WHERE id = ?")
public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String nickname;

    @Column(nullable = false)
    private Long providerId;

    @Column(nullable = false)
    private String provider;

    public User(String nickname, Long providerId, String provider) {
        this.nickname = nickname;
        this.providerId = providerId;
        this.provider = provider;
    }
}
