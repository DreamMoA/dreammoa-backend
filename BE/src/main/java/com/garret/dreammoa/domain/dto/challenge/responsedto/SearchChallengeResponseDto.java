package com.garret.dreammoa.domain.dto.challenge.responsedto;

import lombok.*;

import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchChallengeResponseDto {
    private List<MyChallengeResponseDto> popularChallenges;
    private List<MyChallengeResponseDto> runningChallenges;
    private List<MyChallengeResponseDto> recruitingChallenges;
}
