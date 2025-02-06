package com.garret.dreammoa.domain.dto.user.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileRequest {

    @NotEmpty(message = "Name은 필수입니다.")
    @Size(min = 2, message = "Name은 최소 2글자 이상이어야 합니다.")
    @Pattern(
            regexp = "^[가-힣]{2,}$|^[A-Za-z]{2,}$",
            message = "Name은 한글로 2글자 이상 또는 영어로 2글자 이상이어야 합니다."
    )
    private String name;

    @NotEmpty(message = "Nickname은 필수입니다.")
    @Size(min = 2, max = 12, message = "Nickname은 2~12글자여야 합니다.")
    @Pattern(
            regexp = "^[가-힣A-Za-z]{2,12}$",
            message = "Nickname은 영어와 한글만 허용되며 2~12글자여야 합니다."
    )
    private String nickname;
}
