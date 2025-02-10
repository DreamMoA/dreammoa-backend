package com.garret.dreammoa.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 사용자 정보를 관리하는 JPA 엔티티
 * - Google 로그인과 일반 로그인을 모두 지원
 */
@Entity
@Table(name = "tb_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동 증가 ID
    private Long id; // 사용자 고유 ID

    @Column(nullable = false, length = 255)
    private String name; // 사용자 이름

    @Column(nullable = false, length = 255)
    private String nickname; // 사용자 닉네임

    @Column(nullable = false, length = 255, unique = true)
    private String email; // 사용자 이메일 (고유)

    @Column(length = 255)
    private String password; // 사용자 비밀번호 (Google 로그인 사용자는 비어 있음)

    private LocalDateTime createdAt; // 사용자 생성 시간
    private LocalDateTime lastLogin; // 마지막 로그인 시간

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private FileEntity profileImage;

    @Column(columnDefinition = "TEXT", nullable = true)
    private String determination;

    // JPA의 @PrePersist를 통해 persist 전에 실행
    @PrePersist
    public void prePersist() {
        this.createdAt = (this.createdAt == null) ? LocalDateTime.now() : this.createdAt;
    }

    @OneToMany(mappedBy = "user", orphanRemoval = true)
    @Builder.Default
    private List<ParticipantEntity> userParticipants = new ArrayList<>();

    // 유저에 참가자 추가하는 메소드
    public void addChallenge(ChallengeEntity challenge) {
        ParticipantEntity participant = ParticipantEntity.builder()
                .user(this)
                .challenge(challenge)
                .build();
       this.userParticipants.add(participant);
    }

    public enum Role {
        USER, ADMIN, Google, Naver, Kakao
    }

    /**
     * Google 로그인 시 비밀번호를 비우고 기본 역할을 USER로 설정
     * @param email 사용자 이메일
     * @param name 사용자 이름
     * @return UserEntity Google 사용자 엔티티
     */
    public static UserEntity createGoogleUser(String email, String name) {
        return UserEntity.builder()
                .email(email)
                .name(name)
                .nickname(name) // 닉네임 기본값은 이름
                .password(null) // 비밀번호 없음
                .role(Role.Google) // 소셜로그인 역할 google로 수정
                .build();
    }
}