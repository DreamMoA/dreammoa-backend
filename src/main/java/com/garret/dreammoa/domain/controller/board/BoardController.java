package com.garret.dreammoa.domain.controller.board;

import com.garret.dreammoa.domain.dto.board.requestdto.BoardRequestDto;
import com.garret.dreammoa.domain.dto.board.responsedto.BoardResponseDto;
import com.garret.dreammoa.domain.model.BoardEntity;
import com.garret.dreammoa.domain.repository.BoardRepository;
import com.garret.dreammoa.domain.service.board.BoardService;
import com.garret.dreammoa.domain.service.like.LikeService;
import com.garret.dreammoa.domain.service.viewcount.ViewCountService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/boards")
@RequiredArgsConstructor
//@CrossOrigin(origins = "http://localhost:5173") // 프론트엔드 도메인
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

    // 카테고리별 게시글 개수 조회 엔드포인트
    @GetMapping("/count/{category}")
    public ResponseEntity<Integer> getBoardCountByCategory(@PathVariable String category) {
        // URL 디코딩 추가 (UTF-8 기준)
        try {
            category = java.net.URLDecoder.decode(category, "UTF-8");
        } catch (Exception e) {
            logger.error("카테고리 디코딩 오류", e);
        }

        String key = "board:count:" + category;
        String countStr = redisTemplate.opsForValue().get(key);
        int count = 0;
        if (countStr != null && !countStr.trim().isEmpty()) {
            try {
                count = Integer.parseInt(countStr.trim());
            } catch (NumberFormatException e) {
                logger.error("Redis에 저장된 게시글 카운터 값이 숫자가 아닙니다. key: {}, value: {}", key, countStr, e);
            }
        }
        return ResponseEntity.ok(count);
    }

    //게시글 조회수순 정렬
    @GetMapping("/sorted-by-views")
    public ResponseEntity<List<BoardResponseDto>> getBoardListSortedByViews() {
        List<BoardResponseDto> list = boardService.getBoardListSortedByViews();
        return ResponseEntity.ok(list);
    }

    //게시글 좋아요순 정렬
    @GetMapping("/sorted-by-likes")
    public ResponseEntity<List<BoardResponseDto>> getBoardListSortedByLikes() {
        // 1) 모든 게시글 조회
        List<BoardEntity> boardList = boardRepository.findAll();

        // 2) 각 게시글에 대해 likeCount를 가져와서 ResponseDto로 변환
        List<BoardResponseDto> responseList = boardList.stream()
                .map(board -> {
                    int likeCount = likeService.getLikeCount(board.getPostId()); // Redis 등에서 호출

                    return BoardResponseDto.builder()
                            .postId(board.getPostId())
                            .userId(board.getUser().getId())
                            .userNickname(board.getUser().getNickname())
                            .category(board.getCategory())
                            .title(board.getTitle())
                            .content(board.getContent())
                            .createdAt(board.getCreatedAt())
                            .updatedAt(board.getUpdatedAt())
                            // DB에 있는 viewCount가 Long 타입이므로, int 변환
                            .viewCount(board.getViewCount().intValue())
                            .likeCount(likeCount)
                            .build();
                })
                // 3) 좋아요 개수 기준으로 내림차순 정렬
                .sorted((dtoA, dtoB) -> Integer.compare(dtoB.getLikeCount(), dtoA.getLikeCount()))
                .collect(Collectors.toList());

        // 4) 결과 반환
        return ResponseEntity.ok(responseList);
    }

    //게시글 댓글 순 정렬
    @GetMapping("/sorted-by-comments")
    public ResponseEntity<List<BoardResponseDto>> getBoardListSortedByComments() {
        List<BoardEntity> boardList = boardRepository.findAll(); //db에 저장된 모든 게시글 조회

        //각 게시글 데이터를 DTO로 매핑
        List<BoardResponseDto> responseList = boardList.stream()
                .map(board -> {
                    int viewCount = viewCountService.getViewCount(board.getPostId());
                    int likeCount = likeService.getLikeCount(board.getPostId());
                    int commentCount = boardService.getCommentCountFromCache(board.getPostId());
                    return BoardResponseDto.builder()
                            .postId(board.getPostId())
                            .userId(board.getUser().getId())
                            .userNickname(board.getUser().getNickname())
                            .category(board.getCategory())
                            .title(board.getTitle())
                            .content(board.getContent())
                            .createdAt(board.getCreatedAt())
                            .updatedAt(board.getUpdatedAt())
                            .viewCount(viewCount)
                            .likeCount(likeCount)
                            .commentCount(commentCount)
                            .build();
                })
                .sorted((a, b) -> Integer.compare(b.getCommentCount(), a.getCommentCount())) //댓글 수 기준 내림차순 정렬
                .collect(Collectors.toList()); //정렬된 스트림을 다시 리스트로 수집

        //responseList : 댓글 수 기준으로 정렬된 게시글 DTO들의 리스트가 저장
        return ResponseEntity.ok(responseList); //응답 생성
    }
}
