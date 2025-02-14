package com.garret.dreammoa.domain.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
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

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "max_Participants")
    private Integer maxParticipants;

    @Column(name = "is_host")
    private Boolean isPrivate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "expire_date")
    private LocalDateTime expireDate;

    @Column(name = "is_active")
    private Boolean isActive;

    private Integer standard;

    @Column(name = "session_id")
    private String sessionId;

    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonManagedReference
    private List<ChallengeTagEntity> challengeTags  = new ArrayList<>();

    @OneToMany(mappedBy = "challenge", orphanRemoval = true)
    @Builder.Default
    private List<ParticipantEntity> challengeParticipants = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = (this.createdAt == null) ? LocalDateTime.now() : this.createdAt;
    }

    @PreUpdate
    public void preUpdate(){ this.updatedAt = LocalDateTime.now(); }

    public void addTag(TagEntity tag) {
        ChallengeTagEntity challengeTag = ChallengeTagEntity.builder()
                .challenge(this)
                .tag(tag)
                .build();
        this.challengeTags.add(challengeTag);
    }

    public void update(String title, String description, Integer maxParticipants,
                       Boolean isPrivate, LocalDateTime startDate,
                       LocalDateTime expireDate, Integer standard) {
        this.title = title;
        this.description = description;
        this.maxParticipants = maxParticipants;
        this.isPrivate = isPrivate;
        this.startDate = startDate;
        this.expireDate = expireDate;
        this.standard = standard;
    }
}
