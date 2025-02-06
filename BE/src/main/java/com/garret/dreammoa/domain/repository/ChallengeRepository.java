package com.garret.dreammoa.domain.repository;

import com.garret.dreammoa.domain.model.ChallengeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChallengeRepository extends JpaRepository<ChallengeEntity,Long> {
}
