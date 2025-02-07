package com.garret.dreammoa.domain.repository;

import com.garret.dreammoa.domain.model.BoardEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardRepository extends JpaRepository<BoardEntity, Long> {

    @Modifying
    @Transactional
    @Query("UPDATE BoardEntity b SET b.viewCount = :viewCount WHERE b.postId = :postId")
    void updateViewCount(Long postId, Long viewCount);

    long countByCategory(BoardEntity.Category category);

    // 최신순 정렬 : 카테고리 필터링 후 생성일자 내림차순 정렬 및 페이징
    Page<BoardEntity> findAllByCategoryOrderByCreatedAtDesc(BoardEntity.Category category, Pageable pageable);

    // category 필드로 필터링한 후, viewCount 내림차순 정렬 및 페이징 처리
    Page<BoardEntity> findAllByCategoryOrderByViewCountDesc(BoardEntity.Category category, Pageable pageable);

    // 좋아요순 정렬 : 카테고리별로 likecount 컬럼 기준 내림차순 정렬 및 페이징
    Page<BoardEntity> findAllByCategoryOrderByLikeCountDesc(BoardEntity.Category category, Pageable pageable);

    // 좋아요 수 증가/감소를 위한 업데이트 메서드
    @Modifying
    @Transactional
    @Query("UPDATE BoardEntity b SET b.likeCount = b.likeCount + 1 WHERE b.postId = :postId")
    void incrementLikeCount(Long postId);

    @Modifying
    @Transactional
    @Query("UPDATE BoardEntity b SET b.likeCount = b.likeCount - 1 WHERE b.postId = :postId")
    void decrementLikeCount(Long postId);


    // 댓글순 정렬 : 카테고리별로 commentcount 컬럼 기준 내림차순 정렬
    Page<BoardEntity> findAllByCategoryOrderByCommentCountDesc(BoardEntity.Category category, Pageable pageable);

    // 댓글 수 증가/감소를 위한 업데이트 메서드
    @Modifying
    @Transactional
    @Query("UPDATE BoardEntity b SET b.commentCount = b.commentCount + 1 WHERE b.postId = :postId")
    void incrementCommentCount(Long postId);

    @Modifying
    @Transactional
    @Query("UPDATE BoardEntity b SET b.commentCount = b.commentCount - 1 WHERE b.postId = :postId")
    void decrementCommentCount(Long postId);

    // DB의 viewCount 컬럼을 기준으로 내림차순 정렬 및 페이징
//    Page<BoardEntity> findAllByOrderByViewCountDesc(BoardEntity.Category category, Pageable pageable);

}
