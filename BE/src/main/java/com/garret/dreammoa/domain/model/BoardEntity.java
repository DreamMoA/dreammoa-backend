package com.garret.dreammoa.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_board")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardEntity {

    // 게시글 ID(PK)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id", columnDefinition = "INT UNSIGNED")
    private Long postId;

    //FK : UserEntity 참조
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    //게시글 카테고리 : '질문','자유'
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private Category category;

    //제목
    @Column(nullable = false, length = 255)
    private String title;

    //내용
    @Column(columnDefinition = "TEXT")
    private String content;

    //작성일
    private LocalDateTime createdAt;

    //수정일
    private LocalDateTime updatedAt;

    //조회수
    @Column(nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private Long viewCount = 0L;

    //좋아요수
    @Column(columnDefinition = "INT DEFAULT 0")
    private int likeCount = 0;

    //댓글수
    @Column(columnDefinition = "INT DEFAULT 0")
    private int commentCount = 0;

    // ✅ 태그 목록 (BoardTagEntity와 연결)
    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<BoardTagEntity> boardTags = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = (this.createdAt == null) ? LocalDateTime.now() : this.createdAt;
        this.updatedAt = LocalDateTime.now();
        this.viewCount = (this.viewCount == null) ? 0L : this.viewCount;
        this.likeCount = 0;
        this.commentCount = 0;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 카테고리를 위한 enum
     * DB의 ENUM('질문','자유') 와 동일한 이름으로 매핑
     */
    public enum Category {
        질문, 자유
    }
}
