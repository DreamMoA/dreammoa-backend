package com.garret.dreammoa.domain.controller.badge;

import com.garret.dreammoa.domain.dto.badge.response.BadgeResponseDTO;
import com.garret.dreammoa.domain.model.BadgeEntity;
import com.garret.dreammoa.domain.service.badge.BadgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/badges")
@RequiredArgsConstructor
public class BadgeController {

    private final BadgeService badgeService;

    public ResponseEntity<List<BadgeEntity>> getAllBadges() {
        return ResponseEntity.ok(badgeService.getAllBadges());
    }

    @PostMapping("/assign/{badgeId}")
    public ResponseEntity<String> assignBadgeToUser(@PathVariable Long badgeId) {
        badgeService.assignBadgeToUser(badgeId);
        return ResponseEntity.ok("벳지가 성공적으로 부여되었습니다.");
    }

    @GetMapping("/my")
    public ResponseEntity<List<BadgeResponseDTO>> getUserBadges() {
        return ResponseEntity.ok(badgeService.getUserBadges());
    }
}
