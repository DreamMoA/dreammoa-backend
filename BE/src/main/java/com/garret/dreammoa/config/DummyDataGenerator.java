package com.garret.dreammoa.config;

import com.garret.dreammoa.domain.model.ChallengeEntity;
import com.garret.dreammoa.domain.model.ChallengeLogEntity;
import com.garret.dreammoa.domain.model.UserEntity;
import com.garret.dreammoa.domain.repository.ChallengeLogRepository;
import com.garret.dreammoa.domain.repository.ChallengeRepository;
import com.garret.dreammoa.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@Slf4j
@Component
@RequiredArgsConstructor
public class DummyDataGenerator {

    private final ChallengeLogRepository challengeLogRepository;
    private final ChallengeRepository challengeRepository;
    private final UserRepository userRepository;
    private final Random random = new Random();

    @PostConstruct
    @Transactional
    public void generateDummyData() {
        log.info("📢 한 달치 더미 데이터 생성 시작...");

        // 1. 모든 챌린지 및 사용자 조회
        List<UserEntity> users = userRepository.findAll();
        List<ChallengeEntity> challenges = challengeRepository.findAll();

        if (users.isEmpty() || challenges.isEmpty()) {
            log.warn("🚨 사용자 또는 챌린지가 존재하지 않습니다. 더미 데이터 생성을 건너뜁니다.");
            return;
        }

        // 2. 30일치 데이터 생성 (2명의 사용자 × 3개의 챌린지)
        for (int i = 0; i < 30; i++) {
            LocalDate recordAt = LocalDate.now().minusDays(30 - i); // 한 달 전부터 오늘까지

            for (UserEntity user : users) {
                for (ChallengeEntity challenge : challenges) {
                    int pureStudyTime = getRandomMinutes(60, 300); // 1시간~5시간
                    int screenTime = getRandomMinutes(100, 400); // 100~400분

                    ChallengeLogEntity log = ChallengeLogEntity.builder()
                            .challenge(challenge)
                            .user(user)
                            .recordAt(recordAt)
                            .pureStudyTime(pureStudyTime)
                            .screenTime(screenTime)
                            .isSuccess(pureStudyTime >= 120) // 2시간 이상 공부하면 성공
                            .build();

                    challengeLogRepository.save(log);
                }
            }
        }

        log.info("✅ 한 달치 더미 데이터가 성공적으로 생성되었습니다.");
    }

    // 랜덤한 시간(분) 생성
    private int getRandomMinutes(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }
}
