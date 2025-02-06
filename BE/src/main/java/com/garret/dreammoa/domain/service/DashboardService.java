package com.garret.dreammoa.domain.service;


import com.garret.dreammoa.domain.dto.dashboard.request.StudyHistoryDto;
import com.garret.dreammoa.domain.dto.dashboard.request.UpdateDeterminationRequest;
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
import com.garret.dreammoa.domain.dto.dashboard.response.DeterminationResponse;

import java.time.LocalDate;
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
            String challengeName = log.getChallenge().getName();

            // 챌린지 썸네일 조회
            Optional<FileEntity> thumbnailFile = fileRepository.findByRelatedIdAndRelatedType(challengeId, FileEntity.RelatedType.CHALLENGE)
                    .stream().findFirst();
            String thumbnailUrl = thumbnailFile.map(FileEntity::getFileUrl).orElse(null);

            return StudyHistoryDto.builder()
                    .challengeLogId(log.getId())
                    .challengeId(challengeId)
                    .challengeName(challengeName)
                    .recordDate(log.getRecordDate())
                    .pureStudyTime(log.getPureStudyTime())
                    .screenTime(log.getScreenTime())
                    .isSuccess(log.isSuccess())
                    .thumbnailUrl(thumbnailUrl) // 챌린지 썸네일 추가
                    .build();
        }).collect(Collectors.toList());
    }


    /**
     * 사용자 각오 수정
     */
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

    /**
     * 사용자 각오 조회
     */
    public DeterminationResponse getDetermination(String accessToken) {
        if (!jwtUtil.validateToken(accessToken)) {
            throw new RuntimeException("유효하지 않은 Access Token입니다.");
        }
        Long userId = jwtUtil.getUserIdFromToken(accessToken);
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return new DeterminationResponse(user.getDetermination());
    }
}
