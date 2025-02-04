package com.garret.dreammoa.domain.service.like;

public interface LikeService {

    //좋아요 추가
    void addLike(Long postId, Long userId);

    //좋아요 취소
    void removeLike(Long postId, Long userId);

    //특정 게시글의 좋아요 개수 조회
    int getLikeCount(Long postId);

    //사용자가 특정 게시글에 좋아요를 눌렀는지 체크
    boolean isPostLiked(Long postId, Long userId);

    //스케줄러: Redis 데이터를 주기적으로 db(LikeEntity)에 동기화
    void syncLikesToDB();
}
