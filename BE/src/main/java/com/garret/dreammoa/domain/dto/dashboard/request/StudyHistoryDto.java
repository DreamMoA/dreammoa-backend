package com.garret.dreammoa.domain.dto.dashboard.request;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class StudyHistoryDto {
    private Long challengeLogId;
    private Long challengeId;
    private String challengeTitle;
    private LocalDate recordAt;
    private Integer pureStudyTime;
    private Integer screenTime;
    private boolean isSuccess;
    private String thumbnailUrl;
}
