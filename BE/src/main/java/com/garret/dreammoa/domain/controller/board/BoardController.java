package com.garret.dreammoa.domain.controller.board;

import com.garret.dreammoa.domain.dto.board.requestdto.BoardRequestDto;
import com.garret.dreammoa.domain.dto.board.responsedto.BoardResponseDto;
import com.garret.dreammoa.domain.model.BoardEntity;
import com.garret.dreammoa.domain.repository.BoardRepository;
import com.garret.dreammoa.domain.service.board.BoardService;
import com.garret.dreammoa.domain.service.like.LikeService;
import com.garret.dreammoa.domain.service.viewcount.ViewCountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;
    private final ViewCountService viewCountService;

    private final BoardRepository boardRepository;
    private final LikeService likeService;

    private final RedisTemplate<String, String> redisTemplate;

    private final Logger logger = LoggerFactory.getLogger(BoardController.class);

    //게시글 생성
    @PostMapping
    public ResponseEntity<BoardResponseDto> createBoard(@RequestBody BoardRequestDto requestDto) {
        BoardResponseDto responseDto = boardService.createBoard(requestDto);
        return ResponseEntity.ok(responseDto);
    }

    //게시글 상세조회
    @GetMapping("/{postId}")
    public ResponseEntity<BoardResponseDto> getBoard(@PathVariable Long postId) {
        System.out.println("🚀 게시글 조회 - postId: " + postId);

        //Redis에서 조회수 증가(Mysql 반영은 5분마다 자동실행)
        viewCountService.increaseViewCount(postId);

        //게시글 데이터 가져오기
        BoardResponseDto responseDto = boardService.getBoard(postId);

        //Redis에서 현재 조회수 가져와서 프론트로 전달(프론트에서 실시간 반영 가능)
//        int viewCount = viewCountService.getViewCount(postId);

        //조회수를 응답에 추가
//        responseDto.setViewCount(viewCount);

        return ResponseEntity.ok(responseDto);
    }

    //게시글 목록 조회
    @GetMapping
    public ResponseEntity<List<BoardResponseDto>> getBoardList() {
        List<BoardResponseDto> list = boardService.getBoardList();
        return ResponseEntity.ok(list);
    }

    //게시글 수정
    @PutMapping("/{postId}")
    public ResponseEntity<BoardResponseDto> updateBoard(
            @PathVariable Long postId,
            @RequestBody BoardRequestDto requestDto
    ) {
        BoardResponseDto updatedDto = boardService.updateBoard(postId, requestDto);
        return ResponseEntity.ok(updatedDto);
    }

    //게시글 삭제
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deleteBoard(@PathVariable Long postId) {

        boardService.deleteBoard(postId);
        return ResponseEntity.ok().build();
    }

    //전체 게시글 개수 조회
    @GetMapping("/count")
    public ResponseEntity<Integer> getTotalBoardCount() {
        int totalCount = boardService.getTotalBoardCount();
        return ResponseEntity.ok(totalCount);
    }

    @GetMapping("/count/category")
    public ResponseEntity<Integer> getBoardCountByCategory(@RequestParam String category) {
        try {
            // URL 디코딩 후 trim 처리 (UTF-8 기준)
            String decodedCategory = java.net.URLDecoder.decode(category, "UTF-8").trim();
            logger.debug("디코딩 후 category: {}", decodedCategory);
            category = decodedCategory;
        } catch (Exception e) {
            logger.error("카테고리 디코딩 오류", e);
            category = "";
        }

        // 한글 그대로 키 생성 (초기화 시 사용한 키와 동일)
        String key = "board:count:" + category;
        logger.debug("조회할 Redis 키: {}", key);

        String countStr = redisTemplate.opsForValue().get(key);
        logger.debug("Redis에서 반환된 countStr: {}", countStr);

        int count = 0;
        if (countStr != null && !countStr.trim().isEmpty()) {
            try {
                count = Integer.parseInt(countStr.trim());
            } catch (NumberFormatException e) {
                logger.error("Redis에 저장된 게시글 카운터 값이 숫자가 아닙니다. key: {}, value: {}", key, countStr, e);
                count = 0;
            }
        }
        logger.debug("최종 반환 count: {}", count);
        return ResponseEntity.ok(count);
    }

    //게시글 최신순 정렬
    @GetMapping("/sorted-by-newest")
    public ResponseEntity<Page<BoardResponseDto>> getBoardListSortedByNewest(
            @RequestParam(required = false, defaultValue = "자유") String category,
            @PageableDefault(page = 0, size = 7) Pageable pageable) {
        BoardEntity.Category boardCategory = BoardEntity.Category.valueOf(category);
        Page<BoardResponseDto> result = boardService.getBoardListSortedByNewest(pageable, boardCategory);
        return ResponseEntity.ok(result);
    }

    //게시글 조회수순 정렬
    @GetMapping("/sorted-by-views")
    public ResponseEntity<Page<BoardResponseDto>> getBoardListSortedByViews(
            @RequestParam(required = false, defaultValue = "자유") String category,
            @PageableDefault(page = 0, size = 7) Pageable pageable) {
        // 전달받은 category 문자열을 Enum으로 변환
        BoardEntity.Category boardCategory = BoardEntity.Category.valueOf(category);
        Page<BoardResponseDto> result = boardService.getBoardListSortedByViewCount(pageable, boardCategory);
        return ResponseEntity.ok(result);
    }

    //게시글 좋아요순 정렬
    @GetMapping("/sorted-by-likes")
    public ResponseEntity<Page<BoardResponseDto>> getBoardListSortedByLikes(
            @RequestParam(required = false, defaultValue = "자유") String category,
            @PageableDefault(page = 0, size = 7) Pageable pageable) {
        BoardEntity.Category boardCategory = BoardEntity.Category.valueOf(category);
        Page<BoardResponseDto> result = boardService.getBoardListSortedByLikeCount(pageable, boardCategory);
        return ResponseEntity.ok(result);
    }

    //게시글 댓글 순 정렬
    @GetMapping("/sorted-by-comments")
    public ResponseEntity<Page<BoardResponseDto>> getBoardListSortedByComments(
            @RequestParam(required = false, defaultValue = "자유") String category,
            @PageableDefault(page = 0, size = 7) Pageable pageable) {
        BoardEntity.Category boardCategory = BoardEntity.Category.valueOf(category);
        Page<BoardResponseDto> result = boardService.getBoardListSortedByCommentCount(pageable, boardCategory);
        return ResponseEntity.ok(result);
    }

    //태그 검색
    @GetMapping("/search-by-tag")
    public ResponseEntity<Page<BoardResponseDto>> searchByTag(
            @RequestParam String tag,
            @PageableDefault(page = 0, size = 7) Pageable pageable) {
        Page<BoardResponseDto> results = boardService.searchByTag(tag, pageable);
        return ResponseEntity.ok(results);
    }
}