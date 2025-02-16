package com.garret.dreammoa.domain.repository;

import com.garret.dreammoa.domain.model.BadgeEntity;
import com.garret.dreammoa.domain.model.UserBadgeEntity;
import com.garret.dreammoa.domain.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserBadgeRepository extends JpaRepository<UserBadgeEntity, Long> {
    List<UserBadgeEntity> findByUser(UserEntity user);
    boolean existsByUserAndBadge(UserEntity user, BadgeEntity badge);
    boolean existsByUserAndBadge_Name(UserEntity user, String badgeName);

}
