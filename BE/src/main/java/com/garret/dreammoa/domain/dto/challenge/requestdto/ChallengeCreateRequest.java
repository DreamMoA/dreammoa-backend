package com.garret.dreammoa.domain.dto.challenge.requestdto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ChallengeCreateRequest {

    @NotBlank(message = "챌린지 제목은 필수입니다.")
    private String title;

    @NotBlank(message = "설명은 필수입니다.")
    private String description;

    @Min(value = 1, message = "참여 인원은 최소 1명 이상이어야 합니다.")
    private Integer maxParticipants;

    @NotNull
    private Boolean isPrivate;

    @NotEmpty(message = "태그는 최소 1개 이상 필요합니다.")
    private List<String> tags;

    @NotNull(message = "시작 날짜는 필수입니다.")
    private LocalDateTime startDate;

    @NotNull(message = "종료 날짜는 필수입니다.")
    private LocalDateTime expireDate;

    @NotNull(message = "성공 기준 설정은 필수입니다.")
    private Integer standard;
}
