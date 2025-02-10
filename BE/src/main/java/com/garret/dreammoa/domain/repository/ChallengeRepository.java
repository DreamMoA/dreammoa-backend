package com.garret.dreammoa.domain.repository;

import com.garret.dreammoa.domain.model.ChallengeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChallengeRepository extends JpaRepository<ChallengeEntity,Long> {
    Optional<ChallengeEntity> findFirstByHost_Id(Long hostId);
}
