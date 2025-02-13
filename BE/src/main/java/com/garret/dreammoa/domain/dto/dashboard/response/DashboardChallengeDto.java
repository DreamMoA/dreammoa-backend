package com.garret.dreammoa.domain.dto.dashboard.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardChallengeDto {
    private Long challengeId;
    private String title;
    private String thumbnailUrl;
    // 화면을 켠 시간의 합 (총 공부 시간)
    private int totalScreenTime;
    // 순수 공부 시간의 합 (실제 공부 시간)
    private int totalPureStudyTime;
}
