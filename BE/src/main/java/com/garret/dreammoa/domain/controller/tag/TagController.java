package com.garret.dreammoa.domain.controller.tag;

import com.garret.dreammoa.domain.dto.tag.responsedto.TagResponseDto;
import com.garret.dreammoa.domain.service.tag.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    // 태그 전체 조회 API
    @GetMapping("/tags")
    public ResponseEntity<List<TagResponseDto>> getAllTags() {
        List<TagResponseDto> tags = tagService.getAllTags();
        return ResponseEntity.ok(tags);
    }
}
