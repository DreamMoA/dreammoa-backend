package com.garret.dreammoa.domain.repository;

import com.garret.dreammoa.domain.model.FileEntity;
import com.garret.dreammoa.domain.model.FileEntity.RelatedType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface FileRepository extends JpaRepository<FileEntity, Long> {

    List<FileEntity> findByRelatedIdAndRelatedType(Long relatedId, RelatedType relatedType);
    void deleteByFileId(Long fileId);
}