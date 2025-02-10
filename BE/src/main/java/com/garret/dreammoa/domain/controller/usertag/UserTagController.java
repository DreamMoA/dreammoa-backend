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
     * 여러 개의 태그 추가 (DTO 사용)
     */
    @PostMapping
    public ResponseEntity<List<UserTagResponseDto>> addTags(@RequestBody UserTagRequestDto requestDto) {
        Long userId = securityUtil.getCurrentUser().getId();
        List<UserTagResponseDto> createdTags = tagService.addTags(requestDto.getTagNames(), userId);
        return ResponseEntity.ok(createdTags);
    }

    /**
     *  여러 개의 태그 삭제 (배열 지원)
     */
    @DeleteMapping
    public ResponseEntity<String> deleteTags(@RequestBody UserTagRequestDto requestDto) {
        Long userId = securityUtil.getCurrentUser().getId();
        tagService.deleteTagsByNames(requestDto.getTagNames(), userId);
        return ResponseEntity.ok("태그 삭제 완료");
    }
}
