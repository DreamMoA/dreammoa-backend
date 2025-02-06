package com.garret.dreammoa.domain.dto.challenge.requestdto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
@Getter
@Setter
public class ChallengeRequest {

    private String title;

    private String description;

    private Integer maxParticipants;

    private Boolean isPrivate;

    private List<String> tags;

    private LocalDateTime startDate;

    private LocalDateTime expireDate;

    private Integer standard;
}
