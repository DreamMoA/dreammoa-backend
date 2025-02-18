package com.garret.dreammoa.domain.dto.challenge.responsedto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ChallengeInfoResponseDto {
    private Long challengeId;
    private String title;
    private String description;
    private Integer currentParticipants;
    private Integer maxParticipants;
    private LocalDateTime startDate;
    private LocalDateTime expireDate;
    private Boolean isActive;
    private List<String> tags;
    private Integer standard;
    private String thumbnail;
    private String message;
}
