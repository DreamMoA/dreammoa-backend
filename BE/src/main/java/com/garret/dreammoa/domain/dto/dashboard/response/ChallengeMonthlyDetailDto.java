package com.garret.dreammoa.domain.dto.dashboard.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ChallengeMonthlyDetailDto {
    private LocalDate recordAt;  // 기록 날짜
    private int screenTime;      // 해당 날짜의 화면 켠 시간 (분 단위)
    private boolean isSuccess;   // 해당 날짜의 성공 여부
}