package com.garret.dreammoa.domain.service.board;

import com.garret.dreammoa.domain.dto.board.requestdto.BoardRequestDto;
import com.garret.dreammoa.domain.dto.board.responsedto.BoardResponseDto;
import com.garret.dreammoa.domain.model.BoardEntity;
import org.springframework.data.domain.Page;

import java.util.List;

public interface BoardService {

    // 작성
    BoardResponseDto createBoard(BoardRequestDto dto);

    // 조회
    BoardResponseDto getBoard(Long postId);
//    List<BoardResponseDto> getBoardList();
    List<BoardResponseDto> getBoardListSortedByViews();

    // 수정
    BoardResponseDto updateBoard(Long postId, BoardRequestDto dto);

    // 삭제
    void deleteBoard(Long postId);

    //전체 게시글 개수 조회
    int getTotalBoardCount();

    List<BoardResponseDto> getBoardList();

    //카테고리별 게시글 개수 조회
    int getBoardCountByCategory(String category);

    BoardResponseDto getBoardDtoFromCache(Long postId);

    int getCommentCountFromCache(Long postId);

    /**
     * DB의 실제 게시글 개수로 Redis 카운터를 재초기화한다.
     */
    void reinitializeCounters();

//    Page<BoardResponseDto> getBoardList(int page);

//    Page<BoardResponseDto> getBoardList(int page, String category);

}
