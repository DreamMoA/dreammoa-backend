package com.garret.dreammoa.domain.service.comment;

import com.garret.dreammoa.domain.dto.comment.requestdto.CommentRequestDto;
import com.garret.dreammoa.domain.dto.comment.responsedto.CommentResponseDto;

import java.util.List;

public interface CommentService {

    //댓글 작성
    CommentResponseDto createComment(Long postId, CommentRequestDto commentRequestDto);

    //댓글 수정
    CommentResponseDto updateComment(Long postId, Long commentId, CommentRequestDto commentRequestDto);

    //댓글 삭제
    void deleteComment(Long postId, Long commentId);

    //댓글 개수 조회
    int getCommentCount(Long postId);

    //특정 게시글의 모든 댓글 조회
    List<CommentResponseDto> getCommentsByPostId(Long postId);

    // 계층 구조로 댓글 정렬 (대댓글 포함)
    List<CommentResponseDto> getCommentsByPostIdWithHierarchy(Long postId);
}
