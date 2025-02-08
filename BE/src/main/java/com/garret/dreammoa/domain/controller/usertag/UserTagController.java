package com.garret.dreammoa.domain.controller.usertag;

import com.garret.dreammoa.domain.dto.usertag.requestdto.UserTagRequestDto;
import com.garret.dreammoa.domain.dto.usertag.responsedto.UserTagResponseDto;
import com.garret.dreammoa.domain.model.UserEntity;
import com.garret.dreammoa.domain.service.usertag.UserTagService;
import com.garret.dreammoa.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user-tag")  // ✅ 공통 URL 적용
public class UserTagController {
    private final UserTagService tagService;
    private final SecurityUtil securityUtil; // ✅ SecurityUtil 추가

    /**
     * 특정 사용자의 관심사 태그 조회
     */
    @GetMapping
    public ResponseEntity<List<UserTagResponseDto>> getUserTags() {
        UserEntity user = securityUtil.getCurrentUser(); // ✅ 현재 로그인한 사용자 가져오기
        List<UserTagResponseDto> userTags = tagService.getUserTags(user.getId());
        return ResponseEntity.ok(userTags);
    }

    /**
     * 관심사 태그 추가 (현재 로그인한 사용자)
     */
    @PostMapping
    public ResponseEntity<UserTagResponseDto> addTag(@RequestBody UserTagRequestDto tagRequestDto) {
        UserEntity user = securityUtil.getCurrentUser(); // ✅ 현재 로그인한 사용자 가져오기
        UserTagResponseDto createdTag = tagService.addTag(tagRequestDto, user.getId());
        return ResponseEntity.ok(createdTag);
    }

    /**
     * 관심사 태그 삭제 (자신이 추가한 태그만 가능)
     */
    @DeleteMapping("/{tagId}")
    public ResponseEntity<String> deleteTag(@PathVariable Long tagId) {
        UserEntity user = securityUtil.getCurrentUser(); // ✅ 현재 로그인한 사용자 가져오기
        tagService.deleteTag(tagId, user.getId());
        return ResponseEntity.ok("삭제 완료되었습니다.");
    }
}
