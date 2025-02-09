package com.garret.dreammoa.domain.service;


import com.garret.dreammoa.domain.dto.dashboard.request.StudyHistoryDto;
import com.garret.dreammoa.domain.dto.dashboard.request.UpdateDeterminationRequest;
import com.garret.dreammoa.domain.dto.dashboard.response.*;
import com.garret.dreammoa.domain.model.ChallengeLogEntity;
import com.garret.dreammoa.domain.model.FileEntity;
import com.garret.dreammoa.domain.model.UserEntity;
import com.garret.dreammoa.domain.repository.ChallengeLogRepository;
import com.garret.dreammoa.domain.repository.FileRepository;
import com.garret.dreammoa.domain.repository.UserRepository;
import com.garret.dreammoa.utils.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final ChallengeLogRepository challengeLogRepository;
    private final JwtUtil jwtUtil;
    private final FileRepository fileRepository;

    /**
     * 월별 공부 히스토리 조회
     * JWT 토큰에서 사용자 ID를 추출한 후,
     * 지정한 연도와 월의 시작일~종료일 사이의 기록을 조회합니다.
     */
    public List<StudyHistoryDto> getMonthlyStudyHistory(String accessToken, int year, int month) {
        if (!jwtUtil.validateToken(accessToken)) {
            throw new RuntimeException("유효하지 않은 Access Token입니다.");
        }
        Long userId = jwtUtil.getUserIdFromToken(accessToken);
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());

        List<ChallengeLogEntity> logs = challengeLogRepository.findByUser_IdAndRecordDateBetween(userId, startDate, endDate);

        return logs.stream().map(log -> {
            Long challengeId = log.getChallenge().getChallengeId();
            String challengeTitle = log.getChallenge().getTitle();

            // 챌린지 썸네일 조회
            Optional<FileEntity> thumbnailFile = fileRepository.findByRelatedIdAndRelatedType(challengeId, FileEntity.RelatedType.CHALLENGE)
                    .stream().findFirst();
            String thumbnailUrl = thumbnailFile.map(FileEntity::getFileUrl).orElse(null);

            return StudyHistoryDto.builder()
                    .challengeLogId(log.getId())
                    .challengeId(challengeId)
                    .challengeTitle(challengeTitle)
                    .recordDate(log.getRecordDate())
                    .pureStudyTime(log.getPureStudyTime())
                    .screenTime(log.getScreenTime())
                    .isSuccess(log.isSuccess())
                    .thumbnailUrl(thumbnailUrl) // 챌린지 썸네일 추가
                    .build();
        }).collect(Collectors.toList());
    }


     // 사용자 각오 수정
    @Transactional
    public void updateDetermination(String accessToken, UpdateDeterminationRequest request) {
        if (!jwtUtil.validateToken(accessToken)) {
            throw new RuntimeException("유효하지 않은 Access Token입니다.");
        }
        Long userId = jwtUtil.getUserIdFromToken(accessToken);
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        user.setDetermination(request.getDetermination());
        userRepository.save(user);
    }

     // 사용자 각오 조회
    public DeterminationResponse getDetermination(String accessToken) {
        if (!jwtUtil.validateToken(accessToken)) {
            throw new RuntimeException("유효하지 않은 Access Token입니다.");
        }
        Long userId = jwtUtil.getUserIdFromToken(accessToken);
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return new DeterminationResponse(user.getDetermination());
    }

    // 선택한 첼린지 오늘 순공시간, 총공시간
    public ChallengeTodayStatsResponse getTodayStatsForChallenge(String accessToken, Long challengeId) {
        if (!jwtUtil.validateToken(accessToken)) {
            throw new RuntimeException("유효하지 않은 Access Token입니다.");
        }
        Long userId = jwtUtil.getUserIdFromToken(accessToken);
        LocalDate today = LocalDate.now();

        List<ChallengeLogEntity> todayLogs = challengeLogRepository
                .findByUser_IdAndChallenge_ChallengeIdAndRecordDate(userId, challengeId, today);

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
                .findByUser_IdAndChallenge_ChallengeIdAndRecordDateBetween(userId, challengeId, startDate, endDate);

        long totalPureStudyTime = 0L;
        long totalScreenTime = 0L;
        String challengeTitle = null;
        int daysInMonth = endDate.getDayOfMonth(); // 해당 월의 일수

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

    // 선택한 첼린지 한달 총 공시간 총 순공시간
    public ChallengeMonthlyTotalStatsResponse getMonthlyTotalStatsForChallenge(String accessToken, Long challengeId, int year, int month) {
        if (!jwtUtil.validateToken(accessToken)) {
            throw new RuntimeException("유효하지 않은 Access Token입니다.");
        }
        Long userId = jwtUtil.getUserIdFromToken(accessToken);
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());

        List<ChallengeLogEntity> logs = challengeLogRepository
                .findByUser_IdAndChallenge_ChallengeIdAndRecordDateBetween(userId, challengeId, startDate, endDate);

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

    // 가입 일 로 부터 순공 시간 총 공 시간
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
                .findByUser_IdAndRecordDateBetween(userId, joinDate, today);

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


    // 시간 차이 계산 하는 메서드 기준은 유닉스 타임
    private long convertDurationToSeconds(LocalDateTime duration) {
        if (duration == null) return 0L;
        return java.time.Duration.between(LocalDateTime.of(1970, 1, 1, 0, 0), duration).getSeconds();
    }

}
