package com.garret.dreammoa.domain.dto.comment.responsedto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponseDto {
    private Long commentId;        // 댓글 ID
    private Long userId;           // 작성자 ID (회원 고유번호)
    private String nickname;       // 작성자 닉네임 (ERD에 맞춤)
    private String content;        // 댓글 내용
    private LocalDateTime createdAt;  // 작성일시
    private LocalDateTime updatedAt;  // 수정일시
    private Long parentCommentId;  // 대댓글인 경우 부모 댓글 ID (없으면 null)
    private List<CommentResponseDto> replies; // 대댓글 목록
}
