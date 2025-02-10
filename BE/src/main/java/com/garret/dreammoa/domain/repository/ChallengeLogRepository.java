package com.garret.dreammoa.domain.repository;

import com.garret.dreammoa.domain.model.ChallengeLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ChallengeLogRepository extends JpaRepository<ChallengeLogEntity, Long> {
    List<ChallengeLogEntity> findByUser_IdAndRecordAtBetween(Long userId, LocalDate startDate, LocalDate endDate);
    List<ChallengeLogEntity> findByUser_IdAndChallenge_ChallengeIdAndRecordAt(Long userId, Long challengeId, LocalDate recordAt);
    List<ChallengeLogEntity> findByUser_IdAndChallenge_ChallengeIdAndRecordAtBetween(Long userId, Long challengeId, LocalDate startDate, LocalDate endDate);
    boolean existsByUser_IdAndChallenge_ChallengeIdAndRecordAt(Long userId, Long challengeId, LocalDate recordAt);
}
