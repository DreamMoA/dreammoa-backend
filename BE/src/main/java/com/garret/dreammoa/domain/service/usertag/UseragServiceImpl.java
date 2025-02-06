package com.garret.dreammoa.domain.service.usertag;

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

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UseragServiceImpl implements UserTagService{
    private final UserTagRepository userTagRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;

    @Override
    public List<UserTagResponseDto> getAllTags() {
        return userTagRepository.findAll().stream()
                .map(UserTagResponseDto::new)
                .collect(Collectors.toList());
    }

    @Override
    public UserTagResponseDto addTag(UserTagRequestDto dto, Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자 검색 실패"));

        // 1. `TagEntity`(관리 태그)에서 이미 존재하는 태그인지 확인
        if (tagRepository.findByTagName(dto.getTagName()).isPresent()) {
            throw new IllegalStateException("이미 관리되고 있는 태그입니다.");
        }

        // 2. 유저가 이미 추가한 태그인가?(중복 방지)
        if (userTagRepository.existsByUserAndTagName(user, dto.getTagName())) {
            throw new IllegalStateException("이미 추가된 태그입니다.");
        }

        UserTagEntity tag = UserTagEntity.builder()
                .tagName(dto.getTagName())
                .user(user)
                .build();

        userTagRepository.save(tag);

        return new UserTagResponseDto(tag);
    }

    @Override
    public List<UserTagResponseDto> getUserTags(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자 찾기 실패"));

        return userTagRepository.findTagByUser(user).stream()
                .map(UserTagResponseDto::new)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteTag(Long tagId, Long userId) {
        UserTagEntity tag = userTagRepository.findById(tagId)
                .orElseThrow(() -> new EntityNotFoundException("태그 찾기 실패"));

        if (!tag.getUser().getId().equals(userId)) {
            throw new IllegalStateException("자신이 추가한 것만 삭제 가능");
        }

        userTagRepository.delete(tag);
    }
}
