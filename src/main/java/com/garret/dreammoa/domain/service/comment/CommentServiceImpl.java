package com.garret.dreammoa.domain.service.comment;

import com.garret.dreammoa.domain.dto.comment.requestdto.CommentRequestDto;
import com.garret.dreammoa.domain.dto.comment.responsedto.CommentResponseDto;
import com.garret.dreammoa.domain.model.BoardEntity;
import com.garret.dreammoa.domain.model.CommentEntity;
import com.garret.dreammoa.domain.model.UserEntity;
import com.garret.dreammoa.domain.repository.BoardRepository;
import com.garret.dreammoa.domain.repository.CommentRepository;
import com.garret.dreammoa.domain.repository.UserRepository;
import com.garret.dreammoa.domain.service.board.BoardService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService{

    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    // 댓글 작성
    @Override
    @Transactional
    public CommentResponseDto createComment(Long postId, CommentRequestDto commentRequestDto) {
        // 게시글 존재 여부 확인
        BoardEntity board = boardRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        // 현재 로그인된 사용자 정보 가져오기
        UserEntity user = getCurrentUser();

        //부모 댓글이 있는 경우 조회
        CommentEntity parentComment = null;
        if (commentRequestDto.getParentCommentId() != null) {
            parentComment = commentRepository.findById(commentRequestDto.getParentCommentId())
                    .orElseThrow(() -> new RuntimeException("부모 댓글을 찾을 수 없습니다."));

            // 부모 댓글이 이미 대댓글이었다면, 대대댓글을 생성 불가
            if (parentComment.getParentComment() != null) {
                throw new RuntimeException("대댓글에는 추가 댓글을 달 수 없습니다.");
            }
        }

        //댓글 엔티티 생성
        CommentEntity comment = CommentEntity.builder()
                .board(board)
                .user(user)
                .content(commentRequestDto.getContent())
                .parentComment(parentComment)
                .createdAt(LocalDateTime.now())
                .build();

        //댓글 저장
        CommentEntity savedComment = commentRepository.save(comment);

        //ResponseDto로 변환 및 반환
        return convertToResponseDTO(savedComment);
    }

    // 현재 로그인한 사용자 조회
    private UserEntity getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 인증 객체가 null인지 확인
        if (authentication == null || !authentication.isAuthenticated()) {
            System.out.println("인증 정보가 없습니다.");
            throw new EntityNotFoundException("사용자를 찾을 수 없습니다.");
        }

        // 인증된 사용자의 이름(email)이 무엇인지 출력 (디버깅용)
        String email = authentication.getName();
        System.out.println("Authenticated email: " + email);

        // 사용자 조회 시도
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    System.out.println("사용자를 찾을 수 없습니다. 입력된 email: " + email);
                    return new EntityNotFoundException("사용자를 찾을 수 없습니다.");
                });
    }

//    private UserEntity getCurrentUser() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        String username = authentication.getName();
//        return userRepository.findByName(username)
//                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
//    }

    // 댓글 수정
    @Override
    @Transactional
    @PreAuthorize("@commentSecurity.isCommentOwner(#commentId, authentication)")
    public CommentResponseDto updateComment(Long postId, Long commentId, CommentRequestDto commentRequestDto) {
        //게시글 존재 여부 확인
        BoardEntity board = boardRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        //댓글 존재 여부 확인
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));

        // 현재 로그인된 사용자 정보 가져오기
        UserEntity user = getCurrentUser();

        //댓글 작성자 확인
        if (!comment.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("본인이 작성한 댓글만 수정할 수 있습니다.");
        }

        //댓글 내용 수정
        comment.setContent(commentRequestDto.getContent());
        comment.setUpdatedAt(LocalDateTime.now());
        commentRepository.save(comment);

        // DTO 변환 및 반환
        return convertToResponseDTO(comment);
    }

    // 댓글 삭제
    @Override
    @Transactional
    @PreAuthorize("@commentSecurity.isCommentOwner(#commentId, authentication)")
    public void deleteComment(Long postId, Long commentId) {
        //게시글 존재 여부 확인
        BoardEntity board = boardRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        //댓글 존재 여부 확인
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));

        // 현재 로그인된 사용자 정보 가져오기
        UserEntity user = getCurrentUser();


        // 만약 이미 user가 null이면, 즉 "삭제된 댓글"이라면
        if (comment.getUser() == null) {
            // 여기서 바로 return 해주거나,
            // throw new RuntimeException("이미 삭제된 댓글입니다.") 로 안내
            // 예:
            throw new RuntimeException("이미 삭제된 댓글입니다.");
        }

        //댓글 작성자 확인
        if (!comment.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("본인이 작성한 댓글만 삭제할 수 있습니다.");
        }

        if (comment.getReplies().isEmpty()) {
            //대댓글이 없는 경우 → 완전히 삭제
            commentRepository.delete(comment);
        } else {
            //대댓글이 있는 경우 → "삭제된 댓글입니다."로 변경
            comment.setContent("댓글이 삭제되었습니다");
//            comment.setUser(null); // 사용자 정보 제거 (익명화)
            commentRepository.save(comment);
        }
    }

    // 특정 게시글의 모든 댓글 조회 (추가적인 필터링 및 정렬 가능)
    @Override
    @Transactional(readOnly = true)
    public List<CommentResponseDto> getCommentsByPostId(Long postId) {
        // 게시글 존재 여부 확인
        boolean exists = boardRepository.existsById(postId);
        if (!exists) {
            throw new RuntimeException("게시글을 찾을 수 없습니다.");
        }

        // 특정 게시글의 모든 댓글 조회 (UserEntity와 parentComment를 함께 로드)
        List<CommentEntity> comments = commentRepository.findByBoard_PostId(postId);

        // 댓글을 DTO로 변환하여 반환
        return comments.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    // 계층 구조로 댓글을 DTO로 변환하는 메서드 (대댓글 포함)
    private CommentResponseDto convertToResponseDTOWithReplies(CommentEntity comment) {
        CommentResponseDto dto = convertToResponseDTO(comment);
        List<CommentResponseDto> replies = comment.getReplies().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        // 필요한 경우, DTO에 대댓글 목록을 추가할 수 있습니다.
        // 예: dto.setReplies(replies);
        return dto;
    }

    // 특정 게시글의 모든 댓글 조회 (계층 구조 포함)
    @Override
    @Transactional(readOnly = true)
    public List<CommentResponseDto> getCommentsByPostIdWithHierarchy(Long postId) {
        // 게시글 존재 여부 확인
        boolean exists = boardRepository.existsById(postId);
        if (!exists) {
            throw new RuntimeException("게시글을 찾을 수 없습니다.");
        }

        // 특정 게시글의 모든 댓글 조회 (UserEntity와 parentComment, replies를 함께 로드)
        List<CommentEntity> comments = commentRepository.findByBoard_PostId(postId);

        // 디버깅: 중복 여부 확인
        System.out.println("=== 조회된 댓글 목록 ===");
        for (CommentEntity comment : comments) {
            System.out.println("댓글 ID: " + comment.getCommentId() + ", 내용: " + comment.getContent());
        }

        // 최상위 댓글만 필터링하고, 대댓글을 포함하여 DTO로 변환
        return comments.stream()
                .filter(comment -> comment.getParentComment() == null) // 최상위 댓글만 필터링
                .map(this::convertToResponseDtoWithReplies)
                .collect(Collectors.toList());
    }

    // 계층 구조로 댓글을 DTO로 변환하는 메서드 (대댓글 포함)
    private CommentResponseDto convertToResponseDtoWithReplies(CommentEntity comment) {
        List<CommentResponseDto> replies = comment.getReplies().stream()
                .map(this::convertToResponseDtoWithReplies) // 재귀적으로 대댓글도 변환
                .collect(Collectors.toList());

        return CommentResponseDto.builder()
                .commentId(comment.getCommentId())
                .userId(comment.getUser().getId())
                .nickname(comment.getUser().getNickname())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .parentCommentId(comment.getParentComment() != null ? comment.getParentComment().getCommentId() : null)
                .replies(replies)
                .build();
    }

    //DTO 변환
    private CommentResponseDto convertToResponseDTO(CommentEntity savedComment) {
        return CommentResponseDto.builder()
                .commentId(savedComment.getCommentId()) //저장된 댓글의 ID
                .userId(savedComment.getUser().getId()) //댓글을 작성한 사용자의 ID
                .nickname(savedComment.getUser().getNickname()) //댓글 작성자의 닉네임
                .content(savedComment.getContent()) //댓글 내용
                .createdAt(savedComment.getCreatedAt()) //댓글 작성 시간
                .updatedAt(savedComment.getUpdatedAt()) //댓글 수정 시간
                //대댓글인 경우 부모 댓글의 ID, 일반 댓글인 경우 null
                .parentCommentId(savedComment.getParentComment() != null ? savedComment.getParentComment().getCommentId() : null)
                .build(); //CommentResponseDto 인스턴스 생성
    }
}
