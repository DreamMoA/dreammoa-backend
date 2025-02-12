package com.garret.dreammoa.domain.repository;

import com.garret.dreammoa.domain.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByNicknameAndName(String nickname, String name);
    @Query(value = "SELECT determination FROM tb_user " +
            "WHERE determination IS NOT NULL AND TRIM(determination) <> '' " +
            "ORDER BY RAND() LIMIT 7", nativeQuery = true)
    List<String> findRandomDeterminations();

}
