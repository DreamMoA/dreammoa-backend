package com.garret.dreammoa.domain.controller.like;

import com.garret.dreammoa.domain.dto.user.CustomUserDetails;
import com.garret.dreammoa.domain.service.like.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/likes")
public class LikeController {

    private final LikeService likeService;

    // 좋아요 누르기
    @PostMapping("/{postId}")
    public ResponseEntity<String> addLike(
            @PathVariable("postId") Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // userDetails.getId() 로 현재 로그인한 사용자의 ID 추출
        likeService.addLike(postId, userDetails.getId());
        return ResponseEntity.ok("좋아요 완료");
    }

    // 좋아요 취소
    @DeleteMapping("/{postId}")
    public ResponseEntity<String> removeLike(
            @PathVariable("postId") Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        likeService.removeLike(postId, userDetails.getId());
        return ResponseEntity.ok("좋아요 취소");
    }

    @GetMapping("/{postId}/count")
    public ResponseEntity<Integer> getLikeCount(@PathVariable("postId") Long postId){
        int count = likeService.getLikeCount(postId);
        return ResponseEntity.ok(count);
    }

    //사용자가 게시글에 좋아요를 눌렀는지 체크
    @GetMapping("/{postId}/isLiked")
    public ResponseEntity<Boolean> isPostLiked(
            @PathVariable("postId") Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails){
        if (userDetails == null) {
            return ResponseEntity.ok(false); // 로그인하지 않은 경우 false 반환
        }

        boolean isLiked = likeService.isPostLiked(postId, userDetails.getId());
        return ResponseEntity.ok(isLiked);
    }
}
