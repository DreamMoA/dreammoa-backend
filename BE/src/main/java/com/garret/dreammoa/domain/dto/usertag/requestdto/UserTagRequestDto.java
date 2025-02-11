package com.garret.dreammoa.domain.dto.usertag.requestdto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserTagRequestDto {
    @NotEmpty // 빈 리스트 방지
    private List<@NotBlank String> tagNames;
}
