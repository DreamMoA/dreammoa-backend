package com.garret.dreammoa.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 사용자의 챌린지 기록을 저장하는 엔티티
 */
@Entity
@Table(name = "tb_challenge_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ChallengeLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "challenge_log_id", columnDefinition = "INT UNSIGNED")
    private Long id; // 챌린지 기록 고유 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    private ChallengeEntity challenge; // 챌린지 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user; // 챌린지에 참여한 사용자

    @Column(name = "record_at", nullable = false)  // 🔹 주의!
    private LocalDate recordAt; // 챌린지 기록 날짜

    @Column(name = "pure_study_time", nullable = true)
    private Integer pureStudyTime; // 순공 시간

    @Column(name = "screen_time", nullable = true)
    private Integer screenTime; // 화면을 켠 시간

    @Column(name = "is_success", nullable = false)
    @JsonProperty("isSuccess")
    private Boolean isSuccess;

    @PrePersist
    public void prePersist() {
        this.recordAt = (this.recordAt == null) ? LocalDate.now() : this.recordAt;
    }
}
