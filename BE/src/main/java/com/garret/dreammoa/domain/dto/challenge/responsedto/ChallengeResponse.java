package com.garret.dreammoa.domain.dto.challenge.responsedto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.garret.dreammoa.domain.model.ChallengeEntity;
import com.garret.dreammoa.domain.model.ChallengeLogEntity;
import com.garret.dreammoa.domain.model.ChallengeTagEntity;
import com.garret.dreammoa.domain.model.UserEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class ChallengeResponse {

    private Long challengeId;
    private String title;
    private String description;
    private Integer maxParticipants;
    private Boolean isPrivate;
    private LocalDateTime createdAt;
    private LocalDateTime startDate;
    private LocalDateTime expireDate;
    private Boolean isActive;
    private Integer standard;
    private String thumbnail;
    private String message;
    private String token;
    private List<String> challengeTags;

    private Long challengeLogId; // 챌린지 기록 고유 ID
    private LocalDate recordAt; // 챌린지 기록 날짜
    private Integer pureStudyTime; // 순공 시간
    private Integer screenTime; // 화면을 켠 시간

    @JsonProperty("isSuccess")
    private Boolean isSuccess;

    public static ChallengeResponse fromEntity(String thumbnailURL, ChallengeEntity challenge, String message) {
        return ChallengeResponse.builder()
                .challengeId(challenge.getChallengeId())
                .title(challenge.getTitle())
                .description(challenge.getDescription())
                .maxParticipants(challenge.getMaxParticipants())
                .isPrivate(challenge.getIsPrivate())
                .createdAt(challenge.getCreatedAt())
                .startDate(challenge.getStartDate())
                .expireDate(challenge.getExpireDate())
                .isActive(challenge.getIsActive())
                .standard(challenge.getStandard())
                .thumbnail(thumbnailURL)
                .message(message)
                .challengeTags(challenge.getChallengeTags().stream()  // tagName만 추출
                        .map(challengeTag -> challengeTag.getTag().getTagName())
                        .collect(Collectors.toList()))
                .build();
    }

    public static ChallengeResponse fromEntity(ChallengeEntity challenge, String message) {
        return ChallengeResponse.builder()
                .challengeId(challenge.getChallengeId())
                .title(challenge.getTitle())
                .description(challenge.getDescription())
                .maxParticipants(challenge.getMaxParticipants())
                .isPrivate(challenge.getIsPrivate())
                .createdAt(challenge.getCreatedAt())
                .startDate(challenge.getStartDate())
                .expireDate(challenge.getExpireDate())
                .isActive(challenge.getIsActive())
                .standard(challenge.getStandard())
                .thumbnail(null) // 기본값 null 처리
                .message(message)
                .challengeTags(challenge.getChallengeTags().stream()  // tagName만 추출
                        .map(challengeTag -> challengeTag.getTag().getTagName())
                        .collect(Collectors.toList()))
                .build();
    }

    public static ChallengeResponse fromEntity(String thumbnail, ChallengeEntity challenge) {
        return ChallengeResponse.builder()
                .challengeId(challenge.getChallengeId())
                .title(challenge.getTitle())
                .description(challenge.getDescription())
                .maxParticipants(challenge.getMaxParticipants())
                .isPrivate(challenge.getIsPrivate())
                .createdAt(challenge.getCreatedAt())
                .startDate(challenge.getStartDate())
                .expireDate(challenge.getExpireDate())
                .isActive(challenge.getIsActive())
                .standard(challenge.getStandard())
                .thumbnail(thumbnail)
                .message(null)
                .challengeTags(challenge.getChallengeTags().stream()  // tagName만 추출
                        .map(challengeTag -> challengeTag.getTag().getTagName())
                        .collect(Collectors.toList()))
                .build();
    }

    public static ChallengeResponse responseMessage(String message) {
        return ChallengeResponse.builder()
                .message(message)
                .build();
    }

    public static ChallengeResponse responseToken(String message, String token) {
        return ChallengeResponse.builder()
                .message(message)
                .token(token)
                .build();
    }

    public static ChallengeResponse responseTokenWithLog(String message, ChallengeLogEntity challengeLogEntity, String token) {
        return ChallengeResponse.builder()
                .message(message)
                .challengeLogId(challengeLogEntity.getId())
                .recordAt(challengeLogEntity.getRecordAt())
                .pureStudyTime(challengeLogEntity.getPureStudyTime())
                .screenTime(challengeLogEntity.getScreenTime())
                .isSuccess(challengeLogEntity.getIsSuccess())
                .token(token)
                .build();
    }
}
