package com.garret.dreammoa.domain.service.usertag;

import com.garret.dreammoa.domain.dto.tag.requestdto.UserTagRequestDto;
import com.garret.dreammoa.domain.dto.tag.responsedto.UserTagResponseDto;
import com.garret.dreammoa.domain.model.UserEntity;
import com.garret.dreammoa.domain.model.UserTagEntity;
import com.garret.dreammoa.domain.repository.UserRepository;
import com.garret.dreammoa.domain.repository.UserTagRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UseragServiceImpl implements UserTagService{
    private final UserTagRepository tagRepository;
    private final UserRepository userRepository;

    @Override
    public List<UserTagResponseDto> getAllTags() {
        return tagRepository.findAll().stream()
                .map(UserTagResponseDto::new)
                .collect(Collectors.toList());
    }

    @Override
    public UserTagResponseDto addTag(UserTagRequestDto dto, Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자 검색 실패"));

        UserTagEntity tag = UserTagEntity.builder()
                .tagName(dto.getTagName())
                .user(user)
                .build();

        tagRepository.save(tag);

        return new UserTagResponseDto(tag);
    }

    @Override
    public List<UserTagResponseDto> getUserTags(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자 찾기 실패"));

        return tagRepository.findTagByUser(user).stream()
                .map(UserTagResponseDto::new)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteTag(Long tagId, Long userId) {
        UserTagEntity tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new EntityNotFoundException("태그 찾기 실패"));

        if (!tag.getUser().getId().equals(userId)) {
            throw new IllegalStateException("자신이 추가한 것만 삭제 가능");
        }

        tagRepository.delete(tag);
    }
}
