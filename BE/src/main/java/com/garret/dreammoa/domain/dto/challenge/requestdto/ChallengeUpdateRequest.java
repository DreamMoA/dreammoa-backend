package com.garret.dreammoa.domain.dto.challenge.requestdto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChallengeUpdateRequest extends ChallengeRequest{

    @NotNull(message = "챌린지 ID는 필수입니다.")
    private Long challengeId;
}
