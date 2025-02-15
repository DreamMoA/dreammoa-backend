package com.garret.dreammoa.domain.repository;

import com.garret.dreammoa.domain.model.ChallengeEntity;
import com.garret.dreammoa.domain.model.ChallengeLogEntity;
import com.garret.dreammoa.domain.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChallengeLogRepository extends JpaRepository<ChallengeLogEntity, Long> {
    // user 엔티티의 id와 기록 날짜 범위를 기준으로 조회
    List<ChallengeLogEntity> findByUser_IdAndRecordAtBetween(Long userId, LocalDate startDate, LocalDate endDate);
    Optional<ChallengeLogEntity> findByUserAndChallengeAndRecordAt(UserEntity user, ChallengeEntity challenge, LocalDate recordAt);
    List<ChallengeLogEntity> findByUser_IdAndChallenge_ChallengeIdAndRecordAt(Long userId, Long challengeId, LocalDate recordAt);
    List<ChallengeLogEntity> findByUser_IdAndChallenge_ChallengeIdAndRecordAtBetween(Long userId, Long challengeId, LocalDate startDate, LocalDate endDate);
    boolean existsByUser_IdAndChallenge_ChallengeIdAndRecordAt(Long userId, Long challengeId, LocalDate recordAt);
    @Query(value = "SELECT COALESCE(SUM(screen_time), 0) FROM tb_challenge_log", nativeQuery = true)
    Integer findTotalScreenTime();
    List<ChallengeLogEntity> findByUserAndChallenge(UserEntity user, ChallengeEntity challenge);

}
