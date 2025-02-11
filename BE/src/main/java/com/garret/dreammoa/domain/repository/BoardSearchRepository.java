package com.garret.dreammoa.domain.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.DeleteRequest;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import com.garret.dreammoa.domain.document.BoardDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class BoardSearchRepository {

    private final ElasticsearchClient elasticsearchClient; // ✅ Elasticsearch 8.x 클라이언트 사용

    /**
     * 제목 또는 내용에서 키워드가 포함된 게시글 검색(Elastic match 쿼리 사용)
     */
    public List<BoardDocument> searchByKeyword(String keyword) {
        try {
            // ✅ BoolQuery를 사용하여 title과 content 필드를 검색
            Query query = BoolQuery.of(b -> b
                    .should(MatchQuery.of(m -> m.field("title").query(keyword))._toQuery())
                    .should(MatchQuery.of(m -> m.field("content").query(keyword))._toQuery())
            )._toQuery();

            // ✅ Elasticsearch 검색 실행
            var searchResponse = elasticsearchClient.search(s -> s
                            .index("board") // 📌 indexName을 명시적으로 사용해야 함
                            .query(query),
                    BoardDocument.class
            );

            // ✅ 검색 결과 변환 후 반환
            return searchResponse.hits().hits().stream()
                    .map(hit -> hit.source()) // BoardDocument 객체 변환
                    .collect(Collectors.toList());

        } catch (IOException e) {
            throw new RuntimeException("Elasticsearch 검색 중 오류 발생", e);
        }
    }

    /**
     * Elasticsearch에 게시글을 저장 (indexing)
     */
    public void index(BoardDocument boardDocument) {
        try {
            elasticsearchClient.index(IndexRequest.of(i -> i
                    .index("board")
                    .id(String.valueOf(boardDocument.getId()))
                    .document(boardDocument)
            ));
        } catch (IOException e) {
            throw new RuntimeException("Elasticsearch 인덱싱 중 오류 발생", e);
        }
    }

    /**
     * Elasticsearch에서 게시글 삭제
     */
    public void deleteByDocumentId(Long id) {
        try {
            elasticsearchClient.delete(DeleteRequest.of(d -> d
                    .index("board")
                    .id(String.valueOf(id))
            ));
        } catch (IOException e) {
            throw new RuntimeException("Elasticsearch 문서 삭제 중 오류 발생", e);
        }
    }
}