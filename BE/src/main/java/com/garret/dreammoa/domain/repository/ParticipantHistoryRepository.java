package com.garret.dreammoa.domain.repository;

import com.garret.dreammoa.domain.model.ParticipantHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParticipantHistoryRepository extends JpaRepository<ParticipantHistoryRepository, Long> {
    Optional<ParticipantHistoryEntity> findByChallengeIdAndUserIdAndStatus(Long challengeId, Long id, ParticipantHistoryEntity.Status status);
}
