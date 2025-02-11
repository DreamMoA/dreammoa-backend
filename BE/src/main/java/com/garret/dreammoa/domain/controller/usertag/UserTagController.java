package com.garret.dreammoa.domain.controller.usertag;

import com.garret.dreammoa.domain.dto.usertag.requestdto.UserTagRequestDto;
import com.garret.dreammoa.domain.dto.usertag.responsedto.UserTagResponseDto;
import com.garret.dreammoa.domain.service.usertag.UserTagService;
import com.garret.dreammoa.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user-tag")  // 공통 URL 적용
public class UserTagController {
    private final UserTagService tagService;
    private final SecurityUtil securityUtil; // SecurityUtil 추가

    /**
     * 특정 사용자의 관심사 태그 조회
     */
    @GetMapping
    public ResponseEntity<List<UserTagResponseDto>> getUserTags() {
        Long userId = securityUtil.getCurrentUser().getId(); // 현재 로그인한 사용자 가져오기
        List<UserTagResponseDto> userTags = tagService.getUserTags(userId);
        return ResponseEntity.ok(userTags);
    }

    /**
     * 태그 추가를 아예 다 삭제하고 다시 추가하는 느낌으로 (여러개 추가했다 삭제했다 할때 2번 안보내고 한번으로 처리 - FE 편의성)
     */
    @PostMapping
    public ResponseEntity<List<UserTagResponseDto>> resetTags(@RequestBody UserTagRequestDto requestDto) {
        Long userId = securityUtil.getCurrentUser().getId();
        List<UserTagResponseDto> createdTags = tagService.resetTags(requestDto.getTagNames(), userId);
        return ResponseEntity.ok(createdTags);
    }

}
