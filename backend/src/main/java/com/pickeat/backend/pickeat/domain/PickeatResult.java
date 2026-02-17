package com.pickeat.backend.pickeat.domain;

import com.pickeat.backend.global.BaseEntity;
import com.pickeat.backend.restaurant.domain.FoodCategory;
import com.pickeat.backend.restaurant.domain.Restaurant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE pickeat_result SET deleted_at = NOW() WHERE id = ?")
public class PickeatResult extends BaseEntity {

    @Column(nullable = false, unique = true)
    private Long pickeatRecordId;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FoodCategory foodCategory;

    @Column(nullable = false)
    private String roadAddressName;

    @Column(nullable = false)
    private String placeUrl;

    private String tags;
    private String pictureKey;
    private String pictureUrl;

    public PickeatResult(
            Long pickeatRecordId,
            String code,
            String name,
            FoodCategory foodCategory,
            String roadAddressName,
            String placeUrl,
            String tags,
            String pictureKey,
            String pictureUrl
    ) {
        this.pickeatRecordId = pickeatRecordId;
        this.code = code;
        this.name = name;
        this.foodCategory = foodCategory;
        this.roadAddressName = roadAddressName;
        this.placeUrl = placeUrl;
        this.tags = tags;
        this.pictureKey = pictureKey;
        this.pictureUrl = pictureUrl;
    }

    public static PickeatResult from(Long pickeatRecordId, Restaurant restaurant) {
        return new PickeatResult(
                pickeatRecordId,
                restaurant.getCode(),
                restaurant.getName(),
                restaurant.getFoodCategory(),
                restaurant.getRoadAddressName(),
                restaurant.getPlaceUrl(),
                restaurant.getTags(),
                restaurant.getPictureKey(),
                restaurant.getPlaceUrl()
        );
    }
}
