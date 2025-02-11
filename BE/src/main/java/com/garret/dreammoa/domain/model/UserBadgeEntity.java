package com.garret.dreammoa.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_user_badge")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBadgeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_badge_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user; // 사용자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "badge_id", nullable = false)
    private BadgeEntity badge; // 뱃지

    // 여기에 활성화 여부도 있으면 좋지 않을까 싶음(시간 남으면 할 것)
}
