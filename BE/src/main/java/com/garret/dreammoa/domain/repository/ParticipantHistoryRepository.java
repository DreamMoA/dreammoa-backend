package com.garret.dreammoa.domain.repository;

import com.garret.dreammoa.domain.model.ChallengeEntity;
import com.garret.dreammoa.domain.model.ParticipantHistoryEntity;
import com.garret.dreammoa.domain.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParticipantHistoryRepository extends JpaRepository<ParticipantHistoryEntity, Long> {
    Optional<ParticipantHistoryEntity> findByChallengeAndUserAndStatus(ChallengeEntity challenge, UserEntity user, ParticipantHistoryEntity.Status status);
}
