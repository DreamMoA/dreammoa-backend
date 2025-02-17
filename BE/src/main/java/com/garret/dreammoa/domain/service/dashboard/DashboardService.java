package com.garret.dreammoa.domain.service.dashboard;


import com.garret.dreammoa.domain.dto.dashboard.response.*;
import com.garret.dreammoa.domain.model.ChallengeLogEntity;
import com.garret.dreammoa.domain.model.UserEntity;
import com.garret.dreammoa.domain.repository.ChallengeLogRepository;
import com.garret.dreammoa.domain.repository.FileRepository;
import com.garret.dreammoa.domain.repository.UserRepository;
import com.garret.dreammoa.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final ChallengeLogRepository challengeLogRepository;
    private final JwtUtil jwtUtil;
    private final FileRepository fileRepository;


    // 선택한 첼린지 오늘 순공시간, 총공시간
    public ChallengeTodayStatsResponse getTodayStatsForChallenge(String accessToken, Long challengeId) {
        if (!jwtUtil.validateToken(accessToken)) {
            throw new RuntimeException("유효하지 않은 Access Token입니다.");
        }
        Long userId = jwtUtil.getUserIdFromToken(accessToken);
        LocalDate today = LocalDate.now();

        List<ChallengeLogEntity> todayLogs = challengeLogRepository
                .findByUser_IdAndChallenge_ChallengeIdAndRecordAt(userId, challengeId, today);

        long totalPureStudyTime = 0L;
        long totalScreenTime = 0L;
        String challengeTitle = null;

        for (ChallengeLogEntity log : todayLogs) {
            totalPureStudyTime += convertDurationToSeconds(log.getPureStudyTime());
            totalScreenTime += convertDurationToSeconds(log.getScreenTime());
            if (challengeTitle == null) {
                challengeTitle = log.getChallenge().getTitle();
            }
        }

        return ChallengeTodayStatsResponse.builder()
                .challengeId(challengeId)
                .challengeTitle(challengeTitle)
                .totalPureStudyTime(totalPureStudyTime)
                .totalScreenTime(totalScreenTime)
                .build();
    }


    // 선택한 첼린지 한달 평균 총공 시간, 한달 평균 순공 시간
    public ChallengeMonthlyAverageStatsResponse getMonthlyAverageStatsForChallenge(String accessToken, Long challengeId, int year, int month) {
        if (!jwtUtil.validateToken(accessToken)) {
            throw new RuntimeException("유효하지 않은 Access Token입니다.");
        }
        Long userId = jwtUtil.getUserIdFromToken(accessToken);
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());

        List<ChallengeLogEntity> logs = challengeLogRepository
                .findByUser_IdAndChallenge_ChallengeIdAndRecordAtBetween(userId, challengeId, startDate, endDate);

        long totalPureStudyTime = 0L;
        long totalScreenTime = 0L;
        String challengeTitle = null;
        int daysInMonth = endDate.getDayOfMonth();

        for (ChallengeLogEntity log : logs) {
            totalPureStudyTime += convertDurationToSeconds(log.getPureStudyTime());
            totalScreenTime += convertDurationToSeconds(log.getScreenTime());
            if (challengeTitle == null) {
                challengeTitle = log.getChallenge().getTitle();
            }
        }

        long averagePureStudyTime = totalPureStudyTime / daysInMonth;
        long averageScreenTime = totalScreenTime / daysInMonth;

        return ChallengeMonthlyAverageStatsResponse.builder()
                .challengeId(challengeId)
                .challengeTitle(challengeTitle)
                .averagePureStudyTime(averagePureStudyTime)
                .averageScreenTime(averageScreenTime)
                .build();
    }

    // 한달 총합 통계 조회
    public ChallengeMonthlyTotalStatsResponse getMonthlyTotalStatsForChallenge(String accessToken, Long challengeId, int year, int month) {
        if (!jwtUtil.validateToken(accessToken)) {
            throw new RuntimeException("유효하지 않은 Access Token입니다.");
        }
        Long userId = jwtUtil.getUserIdFromToken(accessToken);
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());

        List<ChallengeLogEntity> logs = challengeLogRepository
                .findByUser_IdAndChallenge_ChallengeIdAndRecordAtBetween(userId, challengeId, startDate, endDate);

        long totalPureStudyTime = 0L;
        long totalScreenTime = 0L;
        String challengeTitle = null;

        for (ChallengeLogEntity log : logs) {
            totalPureStudyTime += convertDurationToSeconds(log.getPureStudyTime());
            totalScreenTime += convertDurationToSeconds(log.getScreenTime());
            if (challengeTitle == null) {
                challengeTitle = log.getChallenge().getTitle();
            }
        }

        return ChallengeMonthlyTotalStatsResponse.builder()
                .challengeId(challengeId)
                .challengeTitle(challengeTitle)
                .totalPureStudyTime(totalPureStudyTime)
                .totalScreenTime(totalScreenTime)
                .build();
    }

    // 전체 통계 조회
    public OverallStatsResponse getOverallStats(String accessToken) {
        if (!jwtUtil.validateToken(accessToken)) {
            throw new RuntimeException("유효하지 않은 Access Token입니다.");
        }
        Long userId = jwtUtil.getUserIdFromToken(accessToken);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        LocalDate joinDate = user.getCreatedAt().toLocalDate();
        LocalDate today = LocalDate.now();

        List<ChallengeLogEntity> logs = challengeLogRepository
                .findByUser_IdAndRecordAtBetween(userId, joinDate, today);

        long totalPureStudyTime = 0L;
        long totalScreenTime = 0L;
        for (ChallengeLogEntity log : logs) {
            totalPureStudyTime += convertDurationToSeconds(log.getPureStudyTime());
            totalScreenTime += convertDurationToSeconds(log.getScreenTime());
        }

        return OverallStatsResponse.builder()
                .totalPureStudyTime(totalPureStudyTime)
                .totalScreenTime(totalScreenTime)
                .build();
    }

    public List<DailyStudyTimeDto> getDailyStudyTimeForMonth(String accessToken, int year, int month) {
        if (!jwtUtil.validateToken(accessToken)) {
            throw new RuntimeException("유효하지 않은 Access Token입니다.");
        }
        Long userId = jwtUtil.getUserIdFromToken(accessToken);
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());

        // 해당 월의 학습 로그를 모두 조회
        List<ChallengeLogEntity> logs = challengeLogRepository.findByUser_IdAndRecordAtBetween(userId, startDate, endDate);

        // 날짜별로 그룹핑하고 각 날짜의 총 공부 시간 계산
        Map<LocalDate, Integer> dailyTotals = logs.stream()
                .collect(Collectors.groupingBy(
                        ChallengeLogEntity::getRecordAt,
                        Collectors.summingInt(log -> log.getScreenTime() != null ? log.getScreenTime() : 0)
                        ));

        // 결과를 DTO 리스트로 변환하고 날짜순으로 정렬
        return dailyTotals.entrySet().stream()
                .map(entry -> DailyStudyTimeDto.builder()
                        .recordAt(entry.getKey())
                        .totalStudyTime(entry.getValue())
                        .build())
                .sorted(Comparator.comparing(DailyStudyTimeDto::getRecordAt))
                .collect(Collectors.toList());
    }


    // Integer 반환
    private int convertDurationToSeconds(Integer duration) {
        return (duration == null) ? 0 : duration;
    }

}
