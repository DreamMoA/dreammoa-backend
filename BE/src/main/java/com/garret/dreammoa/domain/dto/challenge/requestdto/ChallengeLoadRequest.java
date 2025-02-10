package com.garret.dreammoa.domain.dto.challenge.requestdto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ChallengeLoadRequest {

    private LocalDate recordDate; // 챌린지 기록 날짜
}
