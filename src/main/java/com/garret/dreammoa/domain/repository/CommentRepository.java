package com.garret.dreammoa.domain.repository;

import com.garret.dreammoa.domain.model.BoardEntity;
import com.garret.dreammoa.domain.model.CommentEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

    @EntityGraph(attributePaths = {"user", "parentComment", "replies"})
    List<CommentEntity> findByBoard_PostId(Long postId);

    // 특정 게시글(BoardEntity)에 달린 댓글 개수를 반환하는 메서드 추가
    int countByBoard(BoardEntity board);
}
