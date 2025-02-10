package com.garret.dreammoa.domain.dto.badge.response;

import com.garret.dreammoa.domain.model.BadgeEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BadgeResponseDTO {
    private Long id;
    private String name;
    private String description;
    private String iconUrl;

    public BadgeResponseDTO(BadgeEntity badge) {
        this.id = badge.getId();
        this.name = badge.getName();
        this.description = badge.getDescription();
        this.iconUrl = badge.getIconUrl();
    }
}