package com.garret.dreammoa.domain.dto.main.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TotalScreenTimeResponseDto {
    private String totalScreenTime;
}
