package com.garret.dreammoa.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_challenge_record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChallengeRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id", columnDefinition = "BIGINT UNSIGNED")
    private Long recordId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user; // 챌린지 수행 사용자

    @Column(name = "challenge_name", nullable = false, length = 255)
    private String challengeName; // 챌린지 이름

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime; // 챌린지 시작 시간

    @Column(name = "end_time")
    private LocalDateTime endTime; // 챌린지 종료 시간

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ChallengeStatus status; // 챌린지 상태

    @PrePersist
    public void prePersist() {
        this.startTime = (this.startTime == null) ? LocalDateTime.now() : this.startTime;
        this.status = (this.status == null) ? ChallengeStatus.IN_PROGRESS : this.status;
    }

    @PreUpdate
    public void preUpdate() {
        if (this.endTime != null) {
            this.status = ChallengeStatus.COMPLETED;
        }
    }

    public enum ChallengeStatus {
        IN_PROGRESS, COMPLETED, FAILED
    }
}