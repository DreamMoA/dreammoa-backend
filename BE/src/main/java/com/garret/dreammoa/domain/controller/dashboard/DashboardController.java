package com.garret.dreammoa.domain.controller.dashboard;

import com.garret.dreammoa.domain.dto.dashboard.request.StudyHistoryDto;
import com.garret.dreammoa.domain.dto.dashboard.request.UpdateDeterminationRequest;
import com.garret.dreammoa.domain.dto.dashboard.response.*;
import com.garret.dreammoa.domain.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.garret.dreammoa.domain.dto.dashboard.request.*;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 월별 공부 히스토리 조회
     * 예: GET /dashboard/history?year=2023&month=1
     */
    @GetMapping("/history")
    public List<StudyHistoryDto> getMonthlyStudyHistory(
            @RequestHeader("Authorization") String accessToken,
            @RequestParam int year,
            @RequestParam int month) {
        // "Bearer " 접두어 제거
        if (accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7);
        }
        return dashboardService.getMonthlyStudyHistory(accessToken, year, month);
    }

    /**
     * 사용자 각오 조회
     * 예: GET /dashboard/determination
     */
    @GetMapping("/determination")
    public com.garret.dreammoa.domain.dto.dashboard.response.DeterminationResponse getDetermination(@RequestHeader("Authorization") String accessToken) {
        if (accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7);
        }
        return dashboardService.getDetermination(accessToken);
    }

    /**
     * 사용자 각오 수정
     * 예: PUT /dashboard/determination
     */
    @PutMapping("/determination")
    public void updateDetermination(
            @RequestHeader("Authorization") String accessToken,
            @RequestBody com.garret.dreammoa.domain.dto.dashboard.request.UpdateDeterminationRequest request) {
        if (accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7);
        }
        dashboardService.updateDetermination(accessToken, request);
    }


    // 선택한 첼린지에 대해 오늘 공부, 화면 사용 조회
    @GetMapping("/challenge/{challengeId}/today-stats")
    public ChallengeTodayStatsResponse getTodayStatsForChallenge(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable Long challengeId) {
        if (accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7);
        }
        return dashboardService.getTodayStatsForChallenge(accessToken, challengeId);
    }

    // 선택한 첼린지에 대해 한달 평균 조회
    @GetMapping("/challenge/{challengeId}/monthly-stats")
    public ChallengeMonthlyAverageStatsResponse getMonthlyAverageStatsForChallenge(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable Long challengeId,
            @RequestParam int year,
            @RequestParam int month) {
        if (accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7);
        }
        return dashboardService.getMonthlyAverageStatsForChallenge(accessToken, challengeId, year, month);
    }

    // 선택한 챌린지 한 달 동안 총 공부 시간 및 총 화면 사용 시간 조회
    @GetMapping("/challenge/{challengeId}/monthly-total-stats")
    public ChallengeMonthlyTotalStatsResponse getMonthlyTotalStatsForChallenge(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable Long challengeId,
            @RequestParam int year,
            @RequestParam int month) {
        if (accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7);
        }
        return dashboardService.getMonthlyTotalStatsForChallenge(accessToken, challengeId, year, month);
    }


    // 가입 후 지금까지 전체 공부,화면 토탈 조회
    @GetMapping("/overall-stats")
    public OverallStatsResponse getOverallStats(@RequestHeader("Authorization") String accessToken) {
        if (accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7);
        }
        return dashboardService.getOverallStats(accessToken);
    }
}