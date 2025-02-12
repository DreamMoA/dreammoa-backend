package com.garret.dreammoa.domain.repository;

import com.garret.dreammoa.domain.model.ChallengeTagEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChallengeTagRepository extends JpaRepository<ChallengeTagEntity, Long> {

    // 1️⃣ 태그 리스트에 해당하는 챌린지 ID 찾기
    @Query("SELECT ct.challenge.id FROM ChallengeTagEntity ct " +
            "WHERE ct.tag.tagName IN :tags " +
            "GROUP BY ct.challenge " +
            "ORDER BY COUNT(ct.tag) DESC")
    List<Long> findChallengeIdsByTags(@Param("tags") List<String> tags, Pageable pageable);

    // 주어진 태그 리스트로 챌린지 ID들 조회
    @Query("SELECT ct.challenge.id FROM ChallengeTagEntity ct WHERE ct.tag.tagName IN :tagList")
    List<Long> findChallengeIdsByTags(@Param("tagList") List<String> tagList);
}
