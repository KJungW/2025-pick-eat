package com.pickeat.backend.template.domain;

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
@SQLDelete(sql = "UPDATE template SET deleted_at = NOW() WHERE id = ?")
public class Template extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Boolean isActive;

    public Template(String name, boolean isActive) {
        this.name = name;
        this.isActive = isActive;
    }
}
