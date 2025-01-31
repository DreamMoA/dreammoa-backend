package com.garret.dreammoa.domain.dto.tag.responsedto;

import com.garret.dreammoa.domain.model.UserTagEntity;
import lombok.Getter;

@Getter
public class UserTagResponseDto {
    private Long id;
    private String name;
    private Long userId;

    public UserTagResponseDto(UserTagEntity tag) {
        this.id = tag.getId();
        this.name = tag.getName();
        this.userId = tag.getUser().getId();
    }
}
