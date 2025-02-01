package com.garret.dreammoa.domain.service.like;

public interface LikeService {

    //좋아요 추가
    void addLike(Long postId, Long userId);

    //좋아요 취소
    void removeLike(Long postId, Long userId);

    //좋아요 개수 조회
    int getLikeCount(Long postId);

    //사용자가 특정 게시글에 좋아요를 눌렀는지 체크
    boolean isPostLiked(Long postId, Long userId);
}
