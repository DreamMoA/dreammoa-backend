package com.garret.dreammoa.domain.repository;

import com.garret.dreammoa.domain.model.ChallengeEntity;
import com.garret.dreammoa.domain.model.ParticipantEntity;
import com.garret.dreammoa.domain.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParticipantRepository extends JpaRepository<ParticipantEntity, Long> {
    long countByChallengeId(Long challengeId);

    boolean existsByChallengeIdAndUserId(Long challengeId, Long id);

    Optional<ParticipantEntity> findByUserAndChallenge(UserEntity user, ChallengeEntity challenge);
}
