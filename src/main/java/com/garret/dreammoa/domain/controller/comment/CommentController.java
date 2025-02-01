package com.garret.dreammoa.domain.controller.comment;

import com.garret.dreammoa.domain.dto.comment.requestdto.CommentRequestDto;
import com.garret.dreammoa.domain.dto.comment.responsedto.CommentResponseDto;
import com.garret.dreammoa.domain.service.comment.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/post/{postId}")
public class CommentController {

    private final CommentService commentService;

    //댓글 작성
    @PostMapping("/comments")
    public ResponseEntity<CommentResponseDto> createComment(
            @PathVariable("postId") Long postId,
            @Validated @RequestBody CommentRequestDto commentRequestDto) {
        CommentResponseDto response = commentService.createComment(postId, commentRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //댓글 수정
    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponseDto> updateComment(
            @PathVariable("postId") Long postId,
            @PathVariable("commentId") Long commentId,
            @Validated @RequestBody CommentRequestDto commentRequestDto){
        CommentResponseDto response = commentService.updateComment(postId, commentId, commentRequestDto);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    //댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable("postId") Long postId,
            @PathVariable("commentId") Long commentId){
        commentService.deleteComment(postId, commentId);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    //특정 게시글의 모든 댓글 조회
    /*
    기능 : 특정 게시글(postId)에 달린 모든 댓글을 평면적인 리스트 형태로 조회
    응답 형식 : 댓글 목록이 순차적으로 나열된 리스트 형태로 반화됨. 대댓글은 별도로 포함되지 않음
     */
    @GetMapping("/comments")
    public ResponseEntity<List<CommentResponseDto>> getComments(
            @PathVariable("postId") Long postId){
        List<CommentResponseDto> comments = commentService.getCommentsByPostId(postId);
        return ResponseEntity.status(HttpStatus.OK).body(comments);
    }

    // 특정 게시글의 모든 댓글 조회 (계층 구조 포함, 대댓글 포함)
    /*
    기능 : 특정 게시글(postId)에 달린 모든 댓글을 계층구조 형태로 조회
    응답 형식 : 최상위 댓글이 리스트로 반환되며, 각 댓글 안에 해당 댓글의 대댓글 목록이 포함
     */
    @GetMapping("/hierarchy")
    public ResponseEntity<List<CommentResponseDto>> getCommentsByPostIdWithHierarchy(
            @PathVariable Long postId) {
        List<CommentResponseDto> comments = commentService.getCommentsByPostIdWithHierarchy(postId);
        return ResponseEntity.ok(comments);
    }

}
