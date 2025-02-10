package com.garret.dreammoa.domain.dto.challenge.requestdto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChallengeKickRequest {

    private Long kickedUserId;
    private String reason;
}
