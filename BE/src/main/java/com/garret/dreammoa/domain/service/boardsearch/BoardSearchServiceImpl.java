package com.garret.dreammoa.domain.service.boardsearch;

import com.garret.dreammoa.domain.document.BoardDocument;
import com.garret.dreammoa.domain.repository.BoardSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardSearchServiceImpl implements BoardSearchService {

    private final BoardSearchRepository boardSearchRepository;

    /**
     * 키워드가 포함된 게시글 검색
     * @param keyword 검색할 키워드
     * @return
     */
    @Override
    public List<BoardDocument> searchBoards(String keyword) {
        return boardSearchRepository.findByTitleContainingOrContentContaining(keyword, keyword);
    }
}
