package com.garret.dreammoa.domain.controller.board;

import com.garret.dreammoa.domain.dto.board.requestdto.BoardRequestDto;
import com.garret.dreammoa.domain.dto.board.responsedto.BoardResponseDto;
import com.garret.dreammoa.domain.service.board.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/boards")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173") // 프론트엔드 도메인
public class BoardController {

    private final BoardService boardService;

    /**
     * 게시글 생성
     * POST /boards
     */
    @PostMapping
    public ResponseEntity<BoardResponseDto> createBoard(@RequestBody BoardRequestDto requestDto) {
        BoardResponseDto responseDto = boardService.createBoard(requestDto);
        return ResponseEntity.ok(responseDto);
    }

    /**
     * READ 단건
     * GET /boards/{postId}
     */
    @GetMapping("/{postId}")
    public ResponseEntity<BoardResponseDto> getBoard(@PathVariable Long postId) {
        BoardResponseDto responseDto = boardService.getBoard(postId);
        return ResponseEntity.ok(responseDto);
    }

    /**
     * READ 전체
     * GET /api/boards
     */
    @GetMapping
    public ResponseEntity<List<BoardResponseDto>> getBoardList() {
        List<BoardResponseDto> list = boardService.getBoardList();
        return ResponseEntity.ok(list);
    }

    /**
     * UPDATE
     * PUT /boards/{postId}
     */
    @PutMapping("/{postId}")
    public ResponseEntity<BoardResponseDto> updateBoard(
            @PathVariable Long postId,
            @RequestBody BoardRequestDto requestDto
    ) {
        BoardResponseDto updatedDto = boardService.updateBoard(postId, requestDto);
        return ResponseEntity.ok(updatedDto);
    }

    /**
     * DELETE
     * DELETE /api/boards/{postId}
     */
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deleteBoard(@PathVariable Long postId) {

        boardService.deleteBoard(postId);
        return ResponseEntity.ok().build();
    }
}
