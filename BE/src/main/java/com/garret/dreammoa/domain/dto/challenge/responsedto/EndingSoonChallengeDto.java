package com.garret.dreammoa.domain.dto.challenge.responsedto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EndingSoonChallengeDto {
    private Long challengeId;
    private String title;
    private String thumbnail;
    private long remainingDays;  // 오늘과 startDate(마감일) 사이의 남은 일 수
}