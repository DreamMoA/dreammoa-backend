package com.garret.dreammoa.domain.repository;

import com.garret.dreammoa.domain.model.ParticipantHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParticipantHistoryRepository extends JpaRepository<ParticipantHistoryEntity, Long> {
    Optional<ParticipantHistoryEntity> findByChallenge_ChallengeIdAndUser_IdAndStatus(Long challengeId, Long id, ParticipantHistoryEntity.Status status);
}
