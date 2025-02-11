package com.garret.dreammoa.domain.dto.dashboard.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChallengeMonthlyTotalStatsResponse {
    private Long challengeId;
    private String challengeTitle;

    private long totalPureStudyTime; // 한 달 동안 해당 채린지에서 공부한 총 시간
    private long totalScreenTime; // 한 달 동안 해당 챌린지에서 화면을 켠 총 시간
}
