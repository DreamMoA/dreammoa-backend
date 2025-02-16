package com.garret.dreammoa.domain.controller.report;

import com.garret.dreammoa.domain.dto.report.request.ReportRequestDto;
import com.garret.dreammoa.domain.dto.report.response.ReportResponseDto;
import com.garret.dreammoa.domain.service.report.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
