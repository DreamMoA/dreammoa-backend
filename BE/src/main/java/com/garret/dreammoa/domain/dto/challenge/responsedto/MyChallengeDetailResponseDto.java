package com.garret.dreammoa.domain.dto.challenge.responsedto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class MyChallengeDetailResponseDto {
    private Long challengeId;
    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime expireDate;
    private Boolean isActive;
    private Integer maxParticipants;
    private Integer standard;
    private List<String> tags; // 챌린지 태그명 리스트
    private HostInfo host;   // 방장(호스트) 정보

    @Data
    public static class HostInfo {
        private Long hostId;
        private String nickname;
        private String profilePictureUrl;
    }
}
