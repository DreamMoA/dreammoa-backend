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

    private Integer maxParticipants;

    private Boolean isPrivate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime startDate;

    private LocalDateTime expireDate;

    private Boolean isActive;

    private Integer standard;

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
    public void addUser(UserEntity user){
        ParticipantEntity participant = ParticipantEntity.builder()
                .challenge(this)
                .user(user)
                .build();
        this.challengeParticipants.add(participant);
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
