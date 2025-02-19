package com.garret.dreammoa.domain.repository;

import com.garret.dreammoa.domain.model.BoardEntity;
import com.garret.dreammoa.domain.model.CommentEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

    @EntityGraph(attributePaths = {"user", "parentComment", "replies"})
    List<CommentEntity> findByBoard_PostId(Long postId);

    // 특정 게시글(BoardEntity)에 달린 댓글 개수를 반환하는 메서드 추가
    int countByBoard(BoardEntity board);

    // 특정 게시글(postId)로 댓글 개수를 조회하는 메서드
    @Query("SELECT COUNT(c) FROM CommentEntity c WHERE c.board.postId = :postId")
    int countByBoard_PostId(@Param("postId") Long postId);

    @Modifying
    @Transactional
    @Query(value = """
    DELETE c FROM tb_comment c
    JOIN (SELECT comment_id 
          FROM tb_comment 
          WHERE parent_comment_id IS NOT NULL 
          AND post_id = :boardId) AS subquery
    ON c.comment_id = subquery.comment_id
    """, nativeQuery = true)
    void deleteChildCommentsByBoard(@Param("boardId") Long boardId);


    @Modifying
    @Transactional
    @Query("DELETE FROM CommentEntity c WHERE c.board = :board")
    void deleteByBoard(@Param("board") BoardEntity board);
}
