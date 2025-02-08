package com.garret.dreammoa.domain.dto.challenge.responsedto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MyChallengeResponseDto {
    private Long challengeId;
    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime expireDate;
    private Boolean isActive;
    private List<String> tags;
    private HostInfo host;

    @Data
    public static class HostInfo {
        private Long hostId;
        private String nickname;
        private String profilePictureUrl;
    }
}
