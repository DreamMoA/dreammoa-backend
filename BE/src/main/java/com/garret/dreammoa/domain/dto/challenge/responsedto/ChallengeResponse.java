package com.garret.dreammoa.domain.dto.challenge.responsedto;

import com.garret.dreammoa.domain.model.ChallengeEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ChallengeResponse {

    private Long challengeId;
    private String title;
    private String description;
    private Integer maxParticipants;
    private Boolean isPrivate;
    private String thumbnail;
    private List<String> tags;
    private LocalDateTime startDate;
    private LocalDateTime expireDate;
    private Integer standard;
    private LocalDateTime createdAt;
    private Boolean isActive;
    private String message;

    public static ChallengeResponse fromEntity(String thumbnailURL, ChallengeEntity challenge, String message) {
        return ChallengeResponse.builder()
                .challengeId(challenge.getChallengeId())
                .title(challenge.getTitle())
                .description(challenge.getDescription())
                .maxParticipants(challenge.getMaxParticipants())
                .isPrivate(challenge.getIsPrivate())
                .thumbnail(thumbnailURL)
                .tags(challenge.getChallengeTags().stream()
                        .map(challengeTag -> challengeTag.getTag().getTagName())
                        .toList())
                .startDate(challenge.getStartDate())
                .expireDate(challenge.getExpireDate())
                .standard(challenge.getStandard())
                .createdAt(challenge.getCreatedAt())
                .isActive(challenge.getIsActive())
                .message(message)
                .build();
    }
    public static ChallengeResponse fromEntity(ChallengeEntity challenge, String message) {
        return ChallengeResponse.builder()
                .challengeId(challenge.getChallengeId())
                .title(challenge.getTitle())
                .description(challenge.getDescription())
                .maxParticipants(challenge.getMaxParticipants())
                .isPrivate(challenge.getIsPrivate())
                .tags(challenge.getChallengeTags().stream()
                        .map(challengeTag -> challengeTag.getTag().getTagName())
                        .toList())
                .startDate(challenge.getStartDate())
                .expireDate(challenge.getExpireDate())
                .standard(challenge.getStandard())
                .createdAt(challenge.getCreatedAt())
                .isActive(challenge.getIsActive())
                .message(message)
                .build();
    }

    public static ChallengeResponse fromEntity(String thumbnail, ChallengeEntity challenge) {
        return ChallengeResponse.builder()
                .challengeId(challenge.getChallengeId())
                .title(challenge.getTitle())
                .description(challenge.getDescription())
                .maxParticipants(challenge.getMaxParticipants())
                .isPrivate(challenge.getIsPrivate())
                .thumbnail(thumbnail)
                .tags(challenge.getChallengeTags().stream()
                        .map(challengeTag -> challengeTag.getTag().getTagName())
                        .toList())
                .startDate(challenge.getStartDate())
                .expireDate(challenge.getExpireDate())
                .standard(challenge.getStandard())
                .createdAt(challenge.getCreatedAt())
                .isActive(challenge.getIsActive())
                .build();
    }

    public static ChallengeResponse responseMessage(String message) {
        return ChallengeResponse.builder()
                .message(message)
                .build();
    }
}
