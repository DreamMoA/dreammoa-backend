package com.garret.dreammoa.domain.repository;

import com.garret.dreammoa.domain.model.BoardEntity;
import com.garret.dreammoa.domain.model.LikeEntity;
import com.garret.dreammoa.domain.model.UserEntity;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<LikeEntity, Long> {
    // 특정 게시글 + 특정 사용자로 좋아요를 찾아서 중복 여부 확인에 사용
    Optional<LikeEntity> findByBoardAndUser(BoardEntity board, UserEntity user);

    @Modifying
    @Transactional
    void deleteByUser_IdAndBoard_PostId(Long userId, Long postId);

    @Modifying
    @Transactional
    @Query("DELETE FROM LikeEntity pl WHERE pl.board = :board")
    void deleteByBoard(@Param("board") BoardEntity board);

}
