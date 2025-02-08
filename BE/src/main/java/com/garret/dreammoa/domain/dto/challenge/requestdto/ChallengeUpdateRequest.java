package com.garret.dreammoa.domain.dto.challenge.requestdto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ChallengeUpdateRequest {

    @NotNull(message = "챌린지 ID는 필수입니다.")
    private Long challengeId;

    @NotBlank(message = "챌린지 제목은 필수입니다.")
    private String title;

    @NotBlank(message = "챌린지 설명은 필수입니다.")
    private String description;

    @NotNull(message = "최대 참가자 수는 필수입니다.")
    private Integer maxParticipants;

    private Boolean isPrivate;

    @NotNull(message = "챌린지 시작일은 필수입니다.")
    private LocalDateTime startDate;

    @NotNull(message = "챌린지 종료일은 필수입니다.")
    private LocalDateTime expireDate;

    @NotNull(message = "기준은 필수입니다.")
    private Integer standard;

    private List<String> tags; // 태그 목록
}
