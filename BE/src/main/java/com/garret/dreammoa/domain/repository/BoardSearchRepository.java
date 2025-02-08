package com.garret.dreammoa.domain.repository;

import com.garret.dreammoa.domain.document.BoardDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface BoardSearchRepository extends ElasticsearchRepository<BoardDocument, Long> {

    // 제목 또는 내용에서 키워드가 포함된 게시글 검색(부분 일치 지원)
    List<BoardDocument> findByTitleContainingOrContentContaining(String title, String content);
}
