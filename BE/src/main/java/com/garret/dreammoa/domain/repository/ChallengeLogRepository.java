package com.garret.dreammoa.domain.repository;

import com.garret.dreammoa.domain.model.ChallengeEntity;
import com.garret.dreammoa.domain.model.ChallengeLogEntity;
import com.garret.dreammoa.domain.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChallengeLogRepository extends JpaRepository<ChallengeLogEntity, Long> {
    // user 엔티티의 id와 기록 날짜 범위를 기준으로 조회
    List<ChallengeLogEntity> findByUser_IdAndRecordAtBetween(Long userId, LocalDate startDate, LocalDate endDate);

    Optional<ChallengeLogEntity> findByUserAndChallengeAndRecordAt(UserEntity user, ChallengeEntity challenge, LocalDate recordDate);
}
