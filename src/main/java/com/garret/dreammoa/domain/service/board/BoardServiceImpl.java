package com.garret.dreammoa.domain.service.board;

import com.garret.dreammoa.domain.dto.board.requestdto.BoardRequestDto;
import com.garret.dreammoa.domain.dto.board.responsedto.BoardResponseDto;
import com.garret.dreammoa.domain.dto.user.CustomUserDetails;
import com.garret.dreammoa.domain.model.BoardEntity;
import com.garret.dreammoa.domain.model.UserEntity;
import com.garret.dreammoa.domain.repository.BoardRepository;
import com.garret.dreammoa.domain.repository.CommentRepository;
import com.garret.dreammoa.domain.repository.UserRepository;
import com.garret.dreammoa.domain.service.viewcount.ViewCountService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository; // UserEntity 조회용
    private final CommentRepository commentRepository;
    private final Logger logger = LoggerFactory.getLogger(BoardServiceImpl.class);
    private final ViewCountService viewCountService;
    private final @Qualifier("boardDtoRedisTemplate") RedisTemplate<String, BoardResponseDto> boardDtoRedisTemplate;
    // 문자열 전용 RedisTemplate (댓글 수와 같은 단순 값을 위한 캐싱)
    private final RedisTemplate<String, String> redisTemplate;

    @PostConstruct
    public void initializeBoardCount() {
        // 전체 게시글 개수 초기화
        long totalCount = boardRepository.count();
        redisTemplate.opsForValue().set("board:count", String.valueOf(totalCount));

        // 카테고리별 초기화 (예: "자유", "질문")
        long freeCount = boardRepository.countByCategory(BoardEntity.Category.자유);
        long questionCount = boardRepository.countByCategory(BoardEntity.Category.질문);
        redisTemplate.opsForValue().set("board:count:자유", String.valueOf(freeCount));
        redisTemplate.opsForValue().set("board:count:질문", String.valueOf(questionCount));

        logger.info("게시글 카운터 초기화 완료: 전체={}, 자유={}, 질문={}", totalCount, freeCount, questionCount);
    }

    //게시글 생성
    @Override
    public BoardResponseDto createBoard(BoardRequestDto dto) {
        //작성자(userId) 조회
        //해당 userId를 가진 사용자가 DB에 존재하는지 확인,
        //없으면 예외 발생
        //있으면 user 변수에 UserEntity를 담는다
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("사용자가 인증되지 않았습니다.");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UserEntity user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("해당 사용자 없음: id=" + userDetails.getId()));

        //category(문자열 "질문" or "자유") -> Enum 변환
        BoardEntity.Category category
                = BoardEntity.Category.valueOf(dto.getCategory()); // valueOf("질문") -> Category.질문

        //엔티티 생성
        BoardEntity board = BoardEntity.builder()
                .user(user)
                .category(category)
                .title(dto.getTitle())
                .content(dto.getContent())
                .build();

        //저장
        BoardEntity saved = boardRepository.save(board);

        // 게시글 생성 후, 전체 카운터와 카테고리별 카운터 업데이트
        redisTemplate.opsForValue().increment("board:count", 1);  // 전체 게시글 개수 증가
        String categoryKey = "board:count:" + saved.getCategory().name();
        redisTemplate.opsForValue().increment(categoryKey, 1);      // 해당 카테고리 개수 증가

        //DTO 변환 후 반환
        return convertToResponseDto(saved, 0);
    }

    //게시글 상세조회
    @Override
    public BoardResponseDto getBoard(Long postId) {

        //DTO는 캐시에서 읽어옴
        BoardResponseDto dto = getBoardDtoFromCache(postId);

        // 댓글 수 업데이트: Redis에서 읽어오기
        int commentCount = getCommentCountFromCache(postId);
        dto.setCommentCount(commentCount);

        // 최신 viewCount 조회
        int updatedViewCount = viewCountService.getViewCount(postId);

        // 캐시된 DTO의 viewCount 값을 최신 값으로 교체하여 응답
        dto.setViewCount(updatedViewCount);

        return dto;
    }


    //게시글 전체 조회
    @Override
    public List<BoardResponseDto> getBoardList() {
        //db에서 게시글 전체 조회
        List<BoardEntity> list = boardRepository.findAll();
        //스트림처리로 각 게시글 dto로 변환
        return list.stream()
                .map(board -> {
                    int viewCount = viewCountService.getViewCount(board.getPostId()); //Redis에서 조회수 가져오기
                    int commentCount = getCommentCountFromCache(board.getPostId());
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
                            .commentCount(commentCount)
                            .build();
                }).collect(Collectors.toList());
    }

    @Override
    public List<BoardResponseDto> getBoardListSortedByViews() {
        List<BoardEntity> list = boardRepository.findAll();

        return list.stream()
                .map(board -> {
                    int viewCount = viewCountService.getViewCount(board.getPostId()); //Redis에서 조회수 가져오기
                    int commentCount = getCommentCountFromCache(board.getPostId());
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
                            .commentCount(commentCount)
                            .build();
                }).sorted((a, b) -> Integer.compare(b.getViewCount(), a.getViewCount()))
                .collect(Collectors.toList());
    }


    /**
     * UPDATE
     */
    @Override
    public BoardResponseDto updateBoard(Long postId, BoardRequestDto dto) {
        // 1) 수정할 게시글 찾기
        BoardEntity board = boardRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글이 존재하지 않습니다. id=" + postId));

        // 2) 현재 로그인한 사용자 ID 가져오기
        Long currentUserId = getCurrentUserId();

        // 3) 작성자와 현재 사용자 비교
        if (!board.getUser().getId().equals(currentUserId)) {
            throw new RuntimeException("본인이 작성한 글만 수정할 수 있습니다.");
        }

        // 4) 수정 내용 적용
        if (dto.getTitle() != null) {
            board.setTitle(dto.getTitle());
        }
        if (dto.getContent() != null) {
            board.setContent(dto.getContent());
        }

        // 5) 저장 후 반환
        BoardEntity updated = boardRepository.save(board);

        // 수정 후 캐시 삭제
        String cacheKey = "board:" + postId;
        boardDtoRedisTemplate.delete(cacheKey);

        int viewCount = viewCountService.getViewCount(postId); // Redis에서 조회수 가져오기
        return convertToResponseDto(updated, viewCount);
    }

    /**
     * DELETE
     */
    @Override
    public void deleteBoard(Long postId) {
        // 1) 삭제할 게시글 찾기
        BoardEntity board = boardRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글이 존재하지 않습니다. id=" + postId));

        // 2) 현재 로그인한 사용자 ID 가져오기
        Long currentUserId = getCurrentUserId();

        // 3) 작성자와 현재 사용자 비교
        if (!board.getUser().getId().equals(currentUserId)) {
            throw new RuntimeException("본인이 작성한 글만 삭제할 수 있습니다.");
        }

        // 4) 삭제 수행
        boardRepository.delete(board);

        // 게시글 삭제 후, 전체 및 카테고리별 Redis 카운터 감소
        redisTemplate.opsForValue().decrement("board:count", 1);  // 전체 게시글 개수 감소
        String categoryKey = "board:count:" + board.getCategory().name();
        redisTemplate.opsForValue().decrement(categoryKey, 1);      // 해당 카테고리 개수 감소
    }

    //board:count로 전체 게시글 개수 조회
    @Override
    public int getTotalBoardCount() {
        String countStr = redisTemplate.opsForValue().get("board:count");
        return (countStr != null) ? Integer.parseInt(countStr) : 0;
    }

    @Override
    public int getBoardCountByCategory(String category) {
        // category가 "자유" 또는 "질문"과 같이 전달된다고 가정
        String key = "board:count:" + category;
        String countStr = redisTemplate.opsForValue().get(key);
        return (countStr != null) ? Integer.parseInt(countStr) : 0;
    }

    /**
     * 게시글을 Redis 캐싱에서 조회(없으면 DB에서 조회 후 캐싱)
     */
    @Override
    public BoardResponseDto getBoardDtoFromCache(Long postId) {
        //캐시 키 생성
        String key = "board:" + postId;
        //캐시에서 데이터 조회
        BoardResponseDto cachedDto = boardDtoRedisTemplate.opsForValue().get(key);
        if (cachedDto != null) {
            log.info("📌 Redis에서 게시글 DTO (postId={}) 를 가져옴", postId);
            return cachedDto;
        }
        //캐시 미스 시 DB에서 조회
        BoardEntity boardEntity = boardRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("❌ 게시글이 존재하지 않습니다. postId=" + postId));

        //최신 조회수 가져오기
        int viewCount = viewCountService.getViewCount(postId);
        //BoardResponseDto로 변환
        BoardResponseDto dto = convertToResponseDto(boardEntity, viewCount);
        //변환된 DTO를 Redis에 저장
        boardDtoRedisTemplate.opsForValue().set(key, dto);
        boardDtoRedisTemplate.expire(key, 10, TimeUnit.MINUTES);
        return dto;
    }


    // Redis에서 댓글 수를 조회(없으면 DB에서 계산 후 캐싱)
    public int getCommentCountFromCache(Long postId) {
        //캐시 키 생성
        String key = "commentCount:" + postId;
        //캐시에서 값 조회
        String countStr = redisTemplate.opsForValue().get(key);
        //캐시 값이 존재하는 경우 처리
        if (countStr != null) { //NULL이 아니라면
            try {
                return Integer.parseInt(countStr); //정수형으로 변환하여 반환
            } catch (NumberFormatException e) {
                // 파싱 실패 시 DB에서 다시 계산
            }
        }
        // 캐시 미스이면 DB에서 계산
        BoardEntity board = boardRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글이 존재하지 않습니다."));
        int count = commentRepository.countByBoard(board); //해당 게시글에 달린 댓글의 수를 DB에서 직접 계산
        //계산한 댓글 수를 캐시에 저장
        redisTemplate.opsForValue().set(key, String.valueOf(count)); //계산된 COUNT 값을 문자열로 변환한 후 캐시에 저장
        redisTemplate.expire(key, 5, TimeUnit.MINUTES); //캐시된 데이터의 유효기간을 5분으로 설정 -> 5분 후에는 캐시가 만료되어 DB에서 다시 최신 값을 조회
        return count;
    }

    /**
     * 현재 로그인한 사용자 ID 가져오기
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("사용자가 인증되지 않았습니다.");
        }

        // SecurityContext에서 CustomUserDetails 객체를 가져옴
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getId();  // User의 ID 가져오기
    }

    /**
     * BoardEntity -> BoardResponseDto 변환
     */
    private BoardResponseDto convertToResponseDto(BoardEntity board, int viewCount) {
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
                .build();
    }

}
