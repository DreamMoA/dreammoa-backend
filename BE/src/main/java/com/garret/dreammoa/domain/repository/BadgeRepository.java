package com.garret.dreammoa.domain.repository;

import com.garret.dreammoa.domain.model.BadgeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BadgeRepository extends JpaRepository<BadgeEntity, Long> {

}
