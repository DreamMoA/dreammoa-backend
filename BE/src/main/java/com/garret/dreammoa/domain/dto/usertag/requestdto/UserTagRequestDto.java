package com.garret.dreammoa.domain.dto.usertag.requestdto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserTagRequestDto {
    @NotBlank
    private String tagName; // 태그명은 안빈것만 일단 체크
}
