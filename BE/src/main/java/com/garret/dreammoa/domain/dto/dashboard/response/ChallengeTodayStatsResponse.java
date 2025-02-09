package com.garret.dreammoa.domain.dto.dashboard.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChallengeTodayStatsResponse {
    private Long challengeId;
    private String challengeTitle;
    private long totalPureStudyTime; // 오늘 총 공부 시간
    private long totalScreenTime; // 오늘 총 화면 사용 시간
}
