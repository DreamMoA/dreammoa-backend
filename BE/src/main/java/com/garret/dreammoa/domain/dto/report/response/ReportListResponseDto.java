package com.garret.dreammoa.domain.dto.report.response;

import com.garret.dreammoa.domain.model.ReportEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ReportListResponseDto {
    private Long reportId;
    private ReportEntity.ReportType reportType;
    private String reportedUser; // 신고된 사용자 이메일
    private String reporter; // 신고한 사용자 이메일
    private LocalDateTime createdAt;
    private String reason;
}