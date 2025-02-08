package com.garret.dreammoa.domain.controller.BoardSearch;

import com.garret.dreammoa.domain.document.BoardDocument;
import com.garret.dreammoa.domain.service.boardsearch.BoardSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("boards/search")
@RequiredArgsConstructor
public class BoardSearchController {

    private final BoardSearchService boardSearchService;

    /**
     * 키워드 기반 게시글 검색 API
     * @param keyword 검색할 키워드
     * @return 검색된 게시글 목록
     */
    @GetMapping
    public ResponseEntity<List<BoardDocument>> searchBoards(@RequestParam String keyword){
        List<BoardDocument> results = boardSearchService.searchBoards(keyword);
        return ResponseEntity.ok(results);
    }
}
