package com.garret.dreammoa.domain.controller.usertag;

import com.garret.dreammoa.domain.dto.tag.requestdto.UserTagRequestDto;
import com.garret.dreammoa.domain.dto.tag.responsedto.UserTagResponseDto;
import com.garret.dreammoa.domain.service.UserService;
import com.garret.dreammoa.domain.service.usertag.UserTagService;
import com.garret.dreammoa.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    private final JwtUtil jwtUtil;
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
    public ResponseEntity<List<UserTagResponseDto>> getUserTags(HttpServletRequest request) {
        String accessToken = getAccessTokenFromRequest(request);

        if (accessToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long userId = jwtUtil.getUserIdFromToken(accessToken);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<UserTagResponseDto> userTags = tagService.getUserTags(userId);
        return ResponseEntity.ok(userTags);
    }

    /**
     * 관심사 태그 추가 (현재 로그인한 사용자)
     */
    @PostMapping("/user-tag")
    public ResponseEntity<UserTagResponseDto> addTag(HttpServletRequest request, @RequestBody UserTagRequestDto tagRequestDto) {
        String accessToken = getAccessTokenFromRequest(request);
        if (accessToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long userId = jwtUtil.getUserIdFromToken(accessToken);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserTagResponseDto createdTag = tagService.addTag(tagRequestDto, userId);
        return ResponseEntity.ok(createdTag);
    }

    /**
     * 관심사 태그 삭제 (자신이 추가한 태그만 가능)
     */
    @DeleteMapping("/user-tag/{tagId}")
    public ResponseEntity<String> deleteTag(@PathVariable Long tagId, HttpServletRequest request) {
        String accessToken = getAccessTokenFromRequest(request);
        if (accessToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long userId = jwtUtil.getUserIdFromToken(accessToken);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        tagService.deleteTag(tagId, userId);
        return ResponseEntity.ok("삭제 완료되었습니다.");
    }

    // 쿠키정보가져오기
    private String getAccessTokenFromRequest(HttpServletRequest request) {
        if (request.getCookies() == null) return null;

        for (var cookie : request.getCookies()) {
            if ("access_token".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

}
