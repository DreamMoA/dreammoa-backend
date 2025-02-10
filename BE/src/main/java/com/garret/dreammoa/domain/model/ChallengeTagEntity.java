package com.garret.dreammoa.domain.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.garret.dreammoa.domain.model.ChallengeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_challenge_tag")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChallengeTagEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "challenge_tag_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    @JsonBackReference
    private ChallengeEntity challenge;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private TagEntity tag;
}
