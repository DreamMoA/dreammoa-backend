package com.garret.dreammoa.domain.repository;

import com.garret.dreammoa.domain.model.ChallengeLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ChallengeLogRepository extends JpaRepository<ChallengeLogEntity, Long> {
    List<ChallengeLogEntity> findByUser_IdAndRecordDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
    List<ChallengeLogEntity> findByUser_IdAndChallenge_ChallengeIdAndRecordDate(Long userId, Long challengeId, LocalDate recordDate);
    List<ChallengeLogEntity> findByUser_IdAndChallenge_ChallengeIdAndRecordDateBetween(Long userId, Long challengeId, LocalDate startDate, LocalDate endDate);
    boolean existsByUser_IdAndChallenge_ChallengeIdAndRecordDate(Long userId, Long challengeId, LocalDate recordDate);
}
