package com.garret.dreammoa.domain.repository;

import com.garret.dreammoa.domain.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByNicknameAndName(String nickname, String name);

}
