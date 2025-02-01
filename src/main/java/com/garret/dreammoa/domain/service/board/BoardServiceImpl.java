package com.garret.dreammoa.domain.service.board;

import com.garret.dreammoa.domain.dto.board.requestdto.BoardRequestDto;
import com.garret.dreammoa.domain.dto.board.responsedto.BoardResponseDto;
import com.garret.dreammoa.domain.dto.user.CustomUserDetails;
import com.garret.dreammoa.domain.model.BoardEntity;
import com.garret.dreammoa.domain.model.UserEntity;
import com.garret.dreammoa.domain.repository.BoardRepository;
import com.garret.dreammoa.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository; // UserEntity 조회용
    private final Logger logger = LoggerFactory.getLogger(BoardServiceImpl.class);

    /**
     * CREATE
     */
    @Override
    public BoardResponseDto createBoard(BoardRequestDto dto) {
        // 1) 작성자(userId) 조회
        //“해당 userId”를 가진 사용자가 DB에 존재하는지 확인,
        //없으면 예외 발생.
        //있으면 user 변수에 UserEntity를 담는다.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("사용자가 인증되지 않았습니다.");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UserEntity user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("해당 사용자 없음: id=" + userDetails.getId()));

        // 2) category(문자열 "질문" or "자유") -> Enum 변환
        BoardEntity.Category category
                = BoardEntity.Category.valueOf(dto.getCategory()); // valueOf("질문") -> Category.질문

        // 3) 엔티티 생성
        BoardEntity board = BoardEntity.builder()
                .user(user)
                .category(category)
                .title(dto.getTitle())
                .content(dto.getContent())
                .build();

        // 4) 저장
        BoardEntity saved = boardRepository.save(board);

        // 5) DTO 변환 후 반환
        return convertToResponseDto(saved);
    }

    /**
     * READ 단건
     */
    @Override
    public BoardResponseDto getBoard(Long postId) {
        logger.info("Fetching board with ID: {}", postId);
        BoardEntity board = boardRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글이 존재하지 않습니다. id=" + postId));
        logger.debug("Fetched BoardEntity: {}", board);
        return convertToResponseDto(board);
    }

    /**
     * READ 전체
     */
    @Override
    public List<BoardResponseDto> getBoardList() {
        List<BoardEntity> list = boardRepository.findAll();
        return list.stream()
                .map(this::convertToResponseDto)
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
        return convertToResponseDto(updated);
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
    private BoardResponseDto convertToResponseDto(BoardEntity board) {
        return BoardResponseDto.builder()
                .postId(board.getPostId())
                .userId(board.getUser().getId())
                .userNickname(board.getUser().getNickname())
                .category(board.getCategory())
                .title(board.getTitle())
                .content(board.getContent())
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .build();
    }

}
