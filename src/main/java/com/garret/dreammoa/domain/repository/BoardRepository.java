package com.garret.dreammoa.domain.repository;

import com.garret.dreammoa.domain.model.BoardEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardRepository extends JpaRepository<BoardEntity, Long> {

    @Modifying
    @Transactional
    @Query("UPDATE BoardEntity b SET b.viewCount = :viewCount WHERE b.postId = :postId")
    void updateViewCount(Long postId, Long viewCount);

    long countByCategory(BoardEntity.Category category);

    //조회수 순으로 모든 게시글 조회
    List<BoardEntity> findAllByOrderByViewCountDesc();
}
