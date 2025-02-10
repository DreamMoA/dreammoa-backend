package com.garret.dreammoa.domain.service.tag;

import com.garret.dreammoa.domain.dto.tag.responsedto.TagResponseDto;
import com.garret.dreammoa.domain.model.ChallengeEntity;
import com.garret.dreammoa.domain.model.TagEntity;

import java.util.List;

public interface TagService {
    // 관리되는 태그 전체 조회
    List<TagResponseDto> getAllTags();
    List<TagEntity> getOrCreateTags(List<String> tagNames);
    void updateTags(ChallengeEntity challenge, List<String> tags);
}
