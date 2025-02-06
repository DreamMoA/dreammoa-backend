package com.garret.dreammoa.domain.service.board;

import com.garret.dreammoa.domain.dto.board.requestdto.BoardRequestDto;
import com.garret.dreammoa.domain.dto.board.responsedto.BoardResponseDto;
import com.garret.dreammoa.domain.model.BoardEntity;

import java.util.List;

public interface BoardService {

    // CREATE
    BoardResponseDto createBoard(BoardRequestDto dto);

    // READ
    BoardResponseDto getBoard(Long postId);
    List<BoardResponseDto> getBoardList();
    List<BoardResponseDto> getBoardListSortedByViews();

    // UPDATE
    BoardResponseDto updateBoard(Long postId, BoardRequestDto dto);

    // DELETE
    void deleteBoard(Long postId);

    //전체 게시글 개수 조회
    int getTotalBoardCount();

    //카테고리별 게시글 개수 조회
    int getBoardCountByCategory(String category);

    BoardResponseDto getBoardDtoFromCache(Long postId);

    int getCommentCountFromCache(Long postId);
}
