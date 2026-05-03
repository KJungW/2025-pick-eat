package com.pickeat.backend.template.domain;

import com.pickeat.backend.global.BaseEntity;
import com.pickeat.backend.restaurant.domain.RestaurantInfo;
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
@SQLDelete(sql = "UPDATE template_wish SET deleted_at = NOW() WHERE id = ?")
public class TemplateWish extends BaseEntity {

    private RestaurantInfo restaurantInfo;

    @Column(nullable = false)
    private Long templateId;

    public TemplateWish(RestaurantInfo restaurantInfo, Long templateId) {
        this.restaurantInfo = restaurantInfo;
        this.templateId = templateId;
    }
}
