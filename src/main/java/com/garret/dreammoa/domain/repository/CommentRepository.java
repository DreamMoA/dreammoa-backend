package com.garret.dreammoa.domain.repository;

import com.garret.dreammoa.domain.model.CommentEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

    @EntityGraph(attributePaths = {"user", "parentComment", "replies"})
    List<CommentEntity> findByBoard_PostId(Long postId);
}
