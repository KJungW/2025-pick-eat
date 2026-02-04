package com.pickeat.backend.restaurant.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Embeddable
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE picture SET deleted_at = NOW() WHERE id = ?")
public class Picture {

    @Column(name = "picture_key")
    private String pictureKey;

    @Column(name = "picture_url")
    private String pictureUrl;
}
