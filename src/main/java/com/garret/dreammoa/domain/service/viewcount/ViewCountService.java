package com.garret.dreammoa.domain.service.viewcount;

import java.util.List;

public interface ViewCountService {

    //게시글 조회 시 조회수 증가
    void increaseViewCount(Long postId);

    //특정 게시글의 조회수 반환
    int getViewCount(Long postId);

    //Redis에 저장된 조회수를 일정 주기마다 mysql에 동기화
    void syncViewCountToDB();

}
