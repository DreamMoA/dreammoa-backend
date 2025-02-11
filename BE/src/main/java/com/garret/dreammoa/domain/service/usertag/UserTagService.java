package com.garret.dreammoa.domain.service.usertag;

import com.garret.dreammoa.domain.dto.usertag.responsedto.UserTagResponseDto;

import java.util.List;

public interface UserTagService {
    // 전체 태그 조회
    List<UserTagResponseDto> getAllTags();

    // ✅ 여러 개의 태그 추가
    List<UserTagResponseDto> resetTags(List<String> tagNames, Long userId);

    // 특정 사용자 태그 조회
    List<UserTagResponseDto> getUserTags(Long userId);

    // ✅ 여러 개의 태그 삭제
    void deleteTagsByNames(List<String> tagNames, Long userId);
}
