package com.garret.dreammoa.domain.repository;

import com.garret.dreammoa.domain.model.BoardEntity;
import com.garret.dreammoa.domain.model.BoardTagEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BoardTagRepository extends JpaRepository<BoardTagEntity, Long> {

    // 특정 게시글과 연결된 태그 리스트 가져오기
    List<BoardTagEntity> findByBoard(BoardEntity board);

    //특정 게시글과 연결된 태그 삭제
    @Transactional
    void deleteByBoard(BoardEntity board);

    //특정 태그명을 포함하는 게시글 id 목록 조회
    @Query("SELECT bt.board.postId FROM BoardTagEntity bt WHERE bt.tag.tagName = :tagName")
    List<Long> findBoardIdsByTagName(@Param("tagName") String tagName);
}
