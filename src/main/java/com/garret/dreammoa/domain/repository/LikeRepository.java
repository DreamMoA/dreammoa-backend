package com.garret.dreammoa.domain.repository;

import com.garret.dreammoa.domain.model.BoardEntity;
import com.garret.dreammoa.domain.model.LikeEntity;
import com.garret.dreammoa.domain.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<LikeEntity, Long> {
    // 특정 게시글 + 특정 사용자로 좋아요를 찾아서 중복 여부 확인에 사용
    Optional<LikeEntity> findByBoardAndUser(BoardEntity board, UserEntity user);

    // 특정 게시글의 좋아요 개수
    int countByBoard(BoardEntity board);
}
