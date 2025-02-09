package com.garret.dreammoa.domain.dto.dashboard.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChallengeMonthlyAverageStatsResponse  {
    private Long challengeId;
    private String challengeTitle;
    private long averagePureStudyTime; // 한 달 동안 하루 평균 공부 시간
    private long averageScreenTime; // 한 달 동안 하루 평균 화면 사용 시간

}
