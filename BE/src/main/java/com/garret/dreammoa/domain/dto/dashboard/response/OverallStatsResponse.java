package com.garret.dreammoa.domain.dto.dashboard.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OverallStatsResponse {
    private Long challengeId;
    private String challengeTitle;
    private long totalPureStudyTime; // 전체 공부 시간
    private long totalScreenTime; // 전체 총 공부 시간
}
