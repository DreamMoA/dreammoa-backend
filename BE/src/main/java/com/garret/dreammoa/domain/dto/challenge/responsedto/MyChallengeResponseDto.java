package com.garret.dreammoa.domain.dto.challenge.responsedto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class MyChallengeResponseDto {
    private Long challengeId;
    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime expireDate;
    private Integer currentParticipants;
    private Integer maxParticipants;
    @JsonProperty("isActive")
    private Boolean isActive;
    private Integer standard;
    private List<String> tags;
    private String thumbnail;
}
