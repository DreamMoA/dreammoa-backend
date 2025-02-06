package com.garret.dreammoa.domain.service.usertag;

import com.garret.dreammoa.domain.dto.usertag.requestdto.UserTagRequestDto;
import com.garret.dreammoa.domain.dto.usertag.responsedto.UserTagResponseDto;

import java.util.List;

public interface UserTagService {
    List<UserTagResponseDto> getAllTags();
    UserTagResponseDto addTag(UserTagRequestDto dto, Long userId);
    List<UserTagResponseDto> getUserTags(Long userId);
    void deleteTag(Long tagId, Long userId);
}
