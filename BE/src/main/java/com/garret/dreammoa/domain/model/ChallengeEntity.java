package com.garret.dreammoa.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_challenge")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChallengeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "challenge_id", columnDefinition = "INT UNSIGNED")
    private Long challengeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    private UserEntity host;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer maxParticipants;

    private Boolean isPrivate;

    @Column(length = 255)
    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime startDate;

    private LocalDateTime expireDate;

    private Boolean isActive;

    private Integer standard;

//    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL, orphanRemoval = true)
//    @Builder.Default
//    private List<ChallengeTagEntity> tags = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = (this.createdAt == null) ? LocalDateTime.now() : this.createdAt;
    }

    @PreUpdate
    public void preUpdate(){ this.updatedAt = LocalDateTime.now(); }

    // 챌린지에 태그 추가하는 편의 메서드
//    public void addTag(TagEntity tag) {
//        ChallengeTagEntity challengeTag = ChallengeTagEntity.builder()
//                .challenge(this)
//                .tag(tag)
//                .build();
//        this.challengeTags.add(challengeTag);
//    }
}
