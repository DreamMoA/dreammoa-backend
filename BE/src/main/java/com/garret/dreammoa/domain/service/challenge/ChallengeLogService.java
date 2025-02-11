package com.garret.dreammoa.domain.service.challenge;

import com.garret.dreammoa.domain.dto.challenge.requestdto.ChallengeExitRequest;
import com.garret.dreammoa.domain.model.ChallengeEntity;
import com.garret.dreammoa.domain.model.ChallengeLogEntity;
import com.garret.dreammoa.domain.model.UserEntity;
import com.garret.dreammoa.domain.repository.ChallengeLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChallengeLogService {

    private final ChallengeLogRepository challengeLogRepository;

    @Transactional
    public void saveStudyLog(UserEntity user, ChallengeEntity challenge, ChallengeExitRequest exitData) {

        LocalDate recordAt = exitData.getRecordAt(); // 클라이언트에서 보낸 기록 날짜
        System.out.println("exitData.isSuccess(): " + exitData.getIsSuccess());
        Optional<ChallengeLogEntity> existingLog = challengeLogRepository
                .findByUserAndChallengeAndRecordAt(user, challenge, recordAt);
        if (existingLog.isPresent()) {
            // ✅ 기존 기록이 있다면 업데이트
            ChallengeLogEntity log = existingLog.get();
            log.setPureStudyTime(exitData.getPureStudyTime()); // 순공 시간 업데이트
            log.setScreenTime(exitData.getScreenTime()); // 화면 켠 시간 업데이트
            log.setIsSuccess(exitData.getIsSuccess()); // 성공 여부 업데이트
            challengeLogRepository.save(log);
        } else {
            // ✅ 기록이 없으면 새로 저장
            ChallengeLogEntity log = ChallengeLogEntity.builder()
                    .user(user)
                    .challenge(challenge)
                    .recordAt(recordAt)
                    .pureStudyTime(exitData.getPureStudyTime())
                    .screenTime(exitData.getScreenTime())
                    .isSuccess(exitData.getIsSuccess())
                    .build();
            challengeLogRepository.save(log);
        }
    }
    public Optional<ChallengeLogEntity> loadStudyLog(UserEntity user, ChallengeEntity challenge, LocalDate recordAt) {
        try {
            return challengeLogRepository.findByUserAndChallengeAndRecordAt(user, challenge, recordAt);
        } catch (DataAccessException e) {
            log.error("학습 기록을 불러오는 중 오류 발생: {}", e.getMessage(), e);
            return Optional.empty(); // 예외 발생 시 빈 Optional 반환
        }
    }
}
