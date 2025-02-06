package com.garret.dreammoa.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_tag")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TagEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String tagName; // 태그 이름
}
