package com.garret.dreammoa.domain.dto.report.response;

import com.garret.dreammoa.domain.model.ReportEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReportDetailResponseDto {
    private Long reportId;
    private ReportEntity.ReportType reportType;
    private String reportedUser; // 신고 대상 이메일
    private String reporter; // 신고한 사용자 이메일
    private String reason;
    private LocalDateTime createdAt;
    private Long postId; // 신고된 게시글 ID (nullable)
    private Long commentId; // 신고된 댓글 ID (nullable)
    private Long challengeId; // 신고된 챌린지 ID (nullable)
}