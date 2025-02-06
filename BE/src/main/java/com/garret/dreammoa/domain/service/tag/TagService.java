package com.garret.dreammoa.domain.service.tag;

import com.garret.dreammoa.domain.dto.tag.responsedto.TagResponseDto;
import java.util.List;

public interface TagService {
    // 관리되는 태그 전체 조회
    List<TagResponseDto> getAllTags();
}
