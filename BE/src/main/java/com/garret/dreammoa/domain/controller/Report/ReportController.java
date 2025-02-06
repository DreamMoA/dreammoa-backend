package com.garret.dreammoa.domain.controller.Report;

import com.garret.dreammoa.domain.dto.report.request.ReportRequestDto;
import com.garret.dreammoa.domain.dto.report.response.ReportDetailResponseDto;
import com.garret.dreammoa.domain.dto.report.response.ReportListResponseDto;
import com.garret.dreammoa.domain.dto.report.response.ReportResponseDto;
import com.garret.dreammoa.domain.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<ReportResponseDto> createReport(@Valid @RequestBody ReportRequestDto reportRequestDto) {
       ReportResponseDto responseDto = reportService.createReport(reportRequestDto);
        return ResponseEntity.ok(responseDto);
    }




}
