package com.garret.dreammoa.domain.repository;

import com.garret.dreammoa.domain.model.ChallengeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChallengeRepository extends JpaRepository<ChallengeEntity,Long> {
    @Query("SELECT c FROM ChallengeEntity c " +
            "JOIN c.challengeTags ct " +
            "LEFT JOIN c.challengeParticipants p " +
            "WHERE ct.tag.tagName IN :tags " +
            "GROUP BY c " +
            "ORDER BY COUNT(p) DESC")
    List<ChallengeEntity> findPopularChallengesByTags(@Param("tags") List<String> tags, Pageable pageable);

    // ⏳ 진행 중인 챌린지 조회
    @Query("SELECT c FROM ChallengeEntity c " +
            "LEFT JOIN c.challengeTags ct " +
            "WHERE (:tags IS NULL OR ct.tag.tagName IN :tags) " +
            "AND (:keyword IS NULL OR c.title LIKE %:keyword% OR c.description LIKE %:keyword%) " +
            "AND c.startDate <= :now AND c.expireDate >= :now " +
            "AND SIZE(c.challengeParticipants) < c.maxParticipants")
    Page<ChallengeEntity> findRunningChallenges(@Param("tags") List<String> tags, @Param("keyword") String keyword, @Param("now") LocalDateTime now, Pageable pageable);


    // 📢 모집 중인 챌린지 조회
    @Query("SELECT c FROM ChallengeEntity c " +
            "LEFT JOIN c.challengeTags ct " +
            "WHERE (:tags IS NULL OR ct.tag.tagName IN :tags) " +
            "AND (:keyword IS NULL OR c.title LIKE %:keyword% OR c.description LIKE %:keyword%) " +
            "AND c.startDate > :now " +
            "AND SIZE(c.challengeParticipants) < c.maxParticipants")
    Page<ChallengeEntity> findRecruitingChallenges(@Param("tags") List<String> tags, @Param("keyword") String keyword, @Param("now") LocalDateTime now, Pageable pageable);


    // 🌟 인기 챌린지 (참가자 많은 순)
    @Query("SELECT c FROM ChallengeEntity c " +
            "WHERE (:tags IS NULL OR EXISTS (SELECT 1 FROM c.challengeTags ct WHERE ct.tag.tagName IN :tags)) " +
            "AND (:keyword IS NULL OR c.title LIKE %:keyword% OR c.description LIKE %:keyword%) " +
            "ORDER BY SIZE(c.challengeParticipants) DESC")
    Page<ChallengeEntity> findPopularChallenges(@Param("tags") List<String> tags, @Param("keyword") String keyword, Pageable pageable);

}
