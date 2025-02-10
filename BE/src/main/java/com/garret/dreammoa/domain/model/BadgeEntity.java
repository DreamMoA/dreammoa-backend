package com.garret.dreammoa.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_badge")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BadgeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "badge_id")
    private Long id;

    @Column(nullable = false, length = 255)
    private String name; // 이름

    @Column(nullable = false, length = 500)
    private String description; // 뱃지 설명(gitHub 느낌)

    @Column(nullable = false, length = 500)
    private String iconUrl; //뱃지 아이콘 URL (한주와 상의 후 결정)

    /*
        File에 Enum을 추가 하면 중복 사진이 많이 생지 않을까 싶음
     */

}
