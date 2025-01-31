package com.garret.dreammoa.domain.dto.tag.responsedto;

import com.garret.dreammoa.domain.model.UserTagEntity;
import lombok.Getter;

@Getter
public class UserTagResponseDto {
    private Long id;
    private String tagName;
    private Long userId;

    public UserTagResponseDto(UserTagEntity tag) {
        this.id = tag.getId();
        this.tagName = tag.getTagName();
        this.userId = tag.getUser().getId();
    }
}
