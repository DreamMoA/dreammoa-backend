package com.garret.dreammoa.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_user_tag")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserTagEntity {
    // 태그 id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Long id;

    // 관심사명
    @Column(nullable = false, length = 255)
    private String tagName;

    // 태그 등록한 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id")
    private UserEntity user;

}
