package com.garret.dreammoa.domain.document;
//Elasticsearch에 저장될 문서 구조 정의

import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.elasticsearch.annotations.Document;

import java.time.LocalDateTime;

@Document(indexName = "board")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardDocument {

    @Id
    private Long id;
    private String title;
    private String content;
    private String category;
    private Long userId;
    private String userNickname;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int viewCount;

}
