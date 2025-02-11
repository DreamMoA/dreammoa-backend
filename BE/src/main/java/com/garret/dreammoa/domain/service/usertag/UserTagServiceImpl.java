package com.garret.dreammoa.domain.service.usertag;

import java.util.*;
import com.garret.dreammoa.domain.dto.usertag.requestdto.UserTagRequestDto;
import com.garret.dreammoa.domain.dto.usertag.responsedto.UserTagResponseDto;
import com.garret.dreammoa.domain.model.UserEntity;
import com.garret.dreammoa.domain.model.UserTagEntity;
import com.garret.dreammoa.domain.repository.TagRepository;
import com.garret.dreammoa.domain.repository.UserRepository;
import com.garret.dreammoa.domain.repository.UserTagRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserTagServiceImpl implements UserTagService {
    private final UserTagRepository userTagRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;

    /**
     * 전체 사용자 태그 조회
     */
    @Override
    public List<UserTagResponseDto> getAllTags() {
        return userTagRepository.findAll().stream()
                .map(UserTagResponseDto::new)
                .collect(Collectors.toList());
    }

    /**
     * 태그 여러 개 추가 (배열 지원)
     */
    @Transactional
    @Override
    public List<UserTagResponseDto> resetTags(List<String> tagNames, Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자 검색 실패"));

        // 기존 태그 전부 삭제
        userTagRepository.deleteByUser(user);

        // 안의 데이터의 중복제거
        List<String> uniqueTagNames = new ArrayList<>(new HashSet<>(tagNames));

        // 새로운 태그 추가
        List<UserTagEntity> newTags = uniqueTagNames.stream()
                .distinct()
                .map(tagName -> UserTagEntity.builder()
                        .tagName(tagName)
                        .user(user)
                        .build())
                .collect(Collectors.toList());

        userTagRepository.saveAll(newTags);

        return newTags.stream().map(UserTagResponseDto::new).collect(Collectors.toList());
    }

    /**
     * 특정 사용자의 태그 조회
     */
    @Override
    public List<UserTagResponseDto> getUserTags(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자 찾기 실패"));

        return userTagRepository.findTagByUser(user).stream()
                .map(UserTagResponseDto::new)
                .collect(Collectors.toList());
    }

    /**
     * 태그 여러 개 삭제 (배열 지원)
     */
    @Transactional
    @Override
    public void deleteTagsByNames(List<String> tagNames, Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자 검색 실패"));

        // 해당 유저가 추가한 태그만 검색
        List<UserTagEntity> userTags = userTagRepository.findTagByUser(user).stream()
                .filter(tag -> tagNames.contains(tag.getTagName())) // 입력한 태그 이름이 포함되는지 확인
                .collect(Collectors.toList());

        if (userTags.isEmpty()) {
            throw new IllegalStateException("삭제할 태그가 없습니다.");
        }

        userTagRepository.deleteAll(userTags);
    }
}
