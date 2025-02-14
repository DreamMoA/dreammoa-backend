package com.garret.dreammoa.domain.dto.dashboard.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class DailyStudyTimeDto {
    private LocalDate recordAt; // 날짜
    private int totalStudyTime; // 해당 날짜의 전체 공부 시간(분)
}
