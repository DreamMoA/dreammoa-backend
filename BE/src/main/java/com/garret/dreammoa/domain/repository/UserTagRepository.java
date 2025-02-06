package com.garret.dreammoa.domain.repository;

import com.garret.dreammoa.domain.model.UserEntity;
import com.garret.dreammoa.domain.model.UserTagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserTagRepository extends JpaRepository<UserTagEntity, Long> {
    // 전체조회
    List<UserTagEntity> findAll();

    // 특정 사용자 조회
    List<UserTagEntity> findTagByUser(UserEntity user);

    // 특정 사용자가 등록한 특정 태그 조회
    Optional<UserTagEntity> findTagByIdAndUser(Long id, UserEntity user);
}
