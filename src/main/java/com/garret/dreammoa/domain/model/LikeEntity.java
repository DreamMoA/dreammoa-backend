package com.garret.dreammoa.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_post_like", uniqueConstraints = {@UniqueConstraint(columnNames = {"post_id", "user_id"})})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LikeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //게시글(FK: post_id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name= "post_id", nullable = false)
    private BoardEntity board;

    //사용자(FK: user_id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
}
