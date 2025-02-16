package com.garret.dreammoa.domain.controller.report;

import com.garret.dreammoa.domain.dto.report.response.ReportDetailResponseDto;
import com.garret.dreammoa.domain.dto.report.response.ReportListResponseDto;
import com.garret.dreammoa.domain.service.report.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/admin/reports") // 🚀 관리자용 신고 관리 API
@RequiredArgsConstructor
@Slf4j
public class AdminReportController {
    private final ReportService reportService;

    @GetMapping
    public ResponseEntity<List<ReportListResponseDto>> getAllReports() {
        List<ReportListResponseDto> reports = reportService.getAllReports();
        return ResponseEntity.ok(reports);
    }


//   특정 신고 상세 조회
    @GetMapping("/{reportId}")
    public ResponseEntity<ReportDetailResponseDto> getReportById(@PathVariable Long reportId) {
        ReportDetailResponseDto reportDetail = reportService.getReportById(reportId);
        return ResponseEntity.ok(reportDetail);
    }


    @PostMapping("/{reportId}/confirm")
    public ResponseEntity<?> confirmReport(@PathVariable Long reportId) {
        log.info("📌 [신고 확인 요청] reportId: {}", reportId);

        try {
            reportService.confirmReport(reportId);
            log.info("✅ [신고 처리 완료] reportId: {}", reportId);
            return ResponseEntity.ok("신고 처리 완료 - 해당 콘텐츠가 삭제되었습니다.");
        } catch (Exception e) {
            log.error("❌ [신고 처리 오류] reportId: {} - {}", reportId, e.getMessage());
            return ResponseEntity.badRequest().body("신고 처리 중 오류 발생: " + e.getMessage());
        }
    }

    // 신고 취소 (신고 테이블에서 삭제)
    @DeleteMapping("/{reportId}/cancel")
    public ResponseEntity<?> cancelReport(@PathVariable Long reportId) {
        log.info("📌 [신고 취소 요청] reportId: {}", reportId);

        try {
            reportService.cancelReport(reportId);
            log.info("✅ [신고 취소 완료] reportId: {}", reportId);
            return ResponseEntity.ok("신고 내역이 삭제되었습니다.");
        } catch (Exception e) {
            log.error("❌ [신고 취소 오류] reportId: {} - {}", reportId, e.getMessage());
            return ResponseEntity.badRequest().body("신고 취소 중 오류 발생: " + e.getMessage());
        }
    }
}
