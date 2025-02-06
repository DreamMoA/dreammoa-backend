package com.garret.dreammoa.domain.dto.report.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportRequestDto {

    @NotNull(message = "신고 타입은 필수입니다. (POST, COMMENT, USER, CHALLENGE)")
    private String reportType; // "POST", "COMMENT", "USER", "CHALLENGE"

    @NotNull(message = "대상 ID는 필수입니다.")
    private Long targetId; // 신고할 대상의 ID

    @NotBlank(message = "신고 사유는 필수입니다.")
    private String reason; // 신고 사유
}