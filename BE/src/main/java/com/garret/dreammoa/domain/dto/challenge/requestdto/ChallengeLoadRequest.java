package com.garret.dreammoa.domain.dto.challenge.requestdto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ChallengeLoadRequest {

    private LocalDate recordAt; // 챌린지 기록 날짜
}
