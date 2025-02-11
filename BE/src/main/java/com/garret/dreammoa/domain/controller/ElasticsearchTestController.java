package com.garret.dreammoa.domain.controller;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.InfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/elasticsearch")
@RequiredArgsConstructor
public class ElasticsearchTestController {

    private final ElasticsearchClient elasticsearchClient;

    @GetMapping("/ping")
    public String checkElasticsearchConnection() {
        try {
            InfoResponse response = elasticsearchClient.info();
            return "Elasticsearch 연결 성공! 버전: " + response.version().number();
        } catch (IOException e) {
            return "Elasticsearch 연결 실패: " + e.getMessage();
        }
    }
}
