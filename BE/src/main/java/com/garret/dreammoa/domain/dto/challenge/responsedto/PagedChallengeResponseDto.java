package com.garret.dreammoa.domain.dto.challenge.responsedto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor
public class PagedChallengeResponseDto<T> {
    private List<T> content;  // 챌린지 데이터 리스트
    private int currentPage;  // 현재 페이지 번호
    private int totalPages;   // 전체 페이지 수
    private long totalElements; // 전체 데이터 개수
    private boolean isLastPage;
}
