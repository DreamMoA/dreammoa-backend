package com.garret.dreammoa.domain.repository;

import com.garret.dreammoa.domain.model.ChallengeEntity;
import com.garret.dreammoa.domain.model.ParticipantEntity;
import com.garret.dreammoa.domain.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface ParticipantRepository extends JpaRepository<ParticipantEntity, Long> {
    long countByChallenge(ChallengeEntity challenge);

    boolean existsByChallengeAndUser(ChallengeEntity challenge, UserEntity user);

    Optional<ParticipantEntity> findByUserAndChallenge(UserEntity user, ChallengeEntity challenge);

    Optional<ParticipantEntity> findTopByChallengeOrderByJoinedAtAsc(ChallengeEntity challenge);

    boolean existsByUserAndChallengeAndIsHost(UserEntity user, ChallengeEntity challenge, boolean isHost);

    long countByChallengeAndIsActiveTrue(ChallengeEntity challenge);

    List<ParticipantEntity> findByUser(UserEntity currentUser);
}
