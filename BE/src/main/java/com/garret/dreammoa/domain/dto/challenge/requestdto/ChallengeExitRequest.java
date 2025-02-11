package com.garret.dreammoa.domain.dto.challenge.requestdto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ChallengeExitRequest {

    private LocalDate recordAt; // 챌린지 기록 날짜

    private Integer pureStudyTime; // 순공 시간

    private Integer screenTime; // 화면을 켠 시간

    @JsonProperty("isSuccess")
    private Boolean isSuccess; // 성공/실패 여부
}
