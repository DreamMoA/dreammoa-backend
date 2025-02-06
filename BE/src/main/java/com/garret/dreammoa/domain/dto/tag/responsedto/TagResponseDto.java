package com.garret.dreammoa.domain.dto.tag.responsedto;

import com.garret.dreammoa.domain.model.TagEntity;
import lombok.Getter;

@Getter
public class TagResponseDto {
    private final Long id;
    private final String tagName;

    public TagResponseDto(TagEntity tag) {
        this.id = tag.getId();
        this.tagName = tag.getTagName();
    }
}
