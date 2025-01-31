package com.garret.dreammoa.domain.controller.usertag;

import com.garret.dreammoa.domain.dto.tag.requestdto.UserTagRequestDto;
import com.garret.dreammoa.domain.dto.tag.responsedto.UserTagResponseDto;
import com.garret.dreammoa.domain.service.usertag.UserTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserTagController {
    private final UserTagService tagService;

    /**
     * 전체 조회
     */
    @GetMapping("/tags")
    public ResponseEntity<List<UserTagResponseDto>> getAllTags() {
        List<UserTagResponseDto> tags = tagService.getAllTags();
        return ResponseEntity.ok(tags);
    }

    /**
     * 특정 사용자의 관심사 태그 조회
     */

    @GetMapping("/user-tag")
    public ResponseEntity<List<UserTagResponseDto>> getUserTags(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        List<UserTagResponseDto> userTags = tagService.getUserTags(userId);
        return ResponseEntity.ok(userTags);
    }

    /**
     * 관심사 태그 추가 (현재 로그인한 사용자)
     */
    @PostMapping
    public ResponseEntity<UserTagResponseDto> addTag(@RequestBody UserTagRequestDto tagRequestDto,
                                                     @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        UserTagResponseDto createdTag = tagService.addTag(tagRequestDto, userId);
        return ResponseEntity.ok(createdTag);
    }

    /**
     * 관심사 태그 삭제 (자신이 추가한 태그만 가능)
     */
    @DeleteMapping("/user-tag/{tagId}")
    public ResponseEntity<Void> deleteTag(
            @PathVariable Long tagId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = Long.parseLong(userDetails.getUsername()); // 현재 로그인한 사용자 ID
        tagService.deleteTag(tagId, userId);
        return ResponseEntity.noContent().build();
    }

}
