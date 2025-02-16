package com.garret.dreammoa.domain.service.report;

import com.garret.dreammoa.domain.dto.report.request.ReportRequestDto;
import com.garret.dreammoa.domain.dto.report.response.ReportDetailResponseDto;
import com.garret.dreammoa.domain.dto.report.response.ReportListResponseDto;
import com.garret.dreammoa.domain.dto.report.response.ReportResponseDto;
import com.garret.dreammoa.domain.model.*;
import com.garret.dreammoa.domain.repository.*;
import com.garret.dreammoa.domain.service.challenge.ParticipantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j // Log4j 사용
public class ReportService {
    private final ReportRepository reportRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final ChallengeRepository challengeRepository;
    private final UserRepository userRepository;
    private final ParticipantService participantService;
    @Transactional
    public ReportResponseDto createReport(ReportRequestDto reportRequestDto) {
        log.info("📌 [신고 요청] 신고 타입: {}, 대상 ID: {}, 신고 사유: {}",
                reportRequestDto.getReportType(), reportRequestDto.getTargetId(), reportRequestDto.getReason());

        // 현재 로그인한 사용자를 신고자로 설정
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.error("🚨 [신고 오류] 인증되지 않은 사용자입니다.");
            throw new RuntimeException("인증되지 않은 사용자입니다.");
        }

        String reporterEmail = authentication.getName();
        UserEntity reporter = userRepository.findByEmail(reporterEmail)
                .orElseThrow(() -> {
                    log.error("❌ [신고 오류] 신고자 이메일({})을 찾을 수 없습니다.", reporterEmail);
                    return new RuntimeException("신고하는 사용자를 찾을 수 없습니다.");
                });

        log.info("✅ [신고자 확인] 신고자: {} (ID: {})", reporter.getEmail(), reporter.getId());

        // 신고 타입 변환
        ReportEntity.ReportType reportType;
        try {
            reportType = ReportEntity.ReportType.valueOf(reportRequestDto.getReportType().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("❌ [신고 오류] 유효하지 않은 신고 타입: {}", reportRequestDto.getReportType(), e);
            throw new RuntimeException("유효하지 않은 신고 타입입니다. (POST, COMMENT, USER, CHALLENGE)");
        }

        ReportEntity reportEntity = ReportEntity.builder()
                .reporter(reporter)
                .reportType(reportType)
                .reason(reportRequestDto.getReason())
                .build();

        // 신고 타입별 처리
        switch (reportType) {
            case POST:
                BoardEntity board = boardRepository.findById(reportRequestDto.getTargetId())
                        .orElseThrow(() -> {
                            log.error("❌ [신고 오류] 신고할 게시글 (ID: {})을 찾을 수 없습니다.", reportRequestDto.getTargetId());
                            return new RuntimeException("신고할 게시글을 찾을 수 없습니다.");
                        });

                // 자기 게시글 신고 불가
                if (board.getUser().getId().equals(reporter.getId())) {
                    log.warn("🚫 [신고 제한] 사용자가 자신의 게시글 (ID: {})을 신고하려고 했습니다.", board.getPostId());
                    throw new RuntimeException("자신의 게시글은 신고할 수 없습니다.");
                }

                log.info("📝 [게시글 신고] 신고 대상: {} (게시글 ID: {})", board.getUser().getEmail(), board.getPostId());

                reportEntity.setReportedPost(board);
                reportEntity.setReportedUser(board.getUser());
                break;

            case COMMENT:
                CommentEntity comment = commentRepository.findById(reportRequestDto.getTargetId())
                        .orElseThrow(() -> {
                            log.error("❌ [신고 오류] 신고할 댓글 (ID: {})을 찾을 수 없습니다.", reportRequestDto.getTargetId());
                            return new RuntimeException("신고할 댓글을 찾을 수 없습니다.");
                        });

                // 자기 댓글 신고 불가
                if (comment.getUser().getId().equals(reporter.getId())) {
                    log.warn("🚫 [신고 제한] 사용자가 자신의 댓글 (ID: {})을 신고하려고 했습니다.", comment.getCommentId());
                    throw new RuntimeException("자신이 작성한 댓글은 신고할 수 없습니다.");
                }

                log.info("📝 [댓글 신고] 신고 대상: {} (댓글 ID: {})", comment.getUser().getEmail(), comment.getCommentId());

                reportEntity.setReportedComment(comment);
                reportEntity.setReportedUser(comment.getUser());
                break;

            case CHALLENGE:
                ChallengeEntity challenge = challengeRepository.findById(reportRequestDto.getTargetId())
                        .orElseThrow(() -> {
                            log.error("❌ [신고 오류] 신고할 챌린지 (ID: {})을 찾을 수 없습니다.", reportRequestDto.getTargetId());
                            return new RuntimeException("신고할 챌린지를 찾을 수 없습니다.");
                        });

                // 참가자 테이블에서 방장 찾기
                ParticipantEntity hostParticipant = participantService.getCurrentHost(reporter, challenge)
                        .orElseThrow(() -> {
                            log.error("❌ [신고 오류] 챌린지에 방장이 없습니다. (챌린지 ID: {})", challenge.getChallengeId());
                            return new RuntimeException("이 챌린지에는 방장이 없습니다.");
                        });

                // 자기 챌린지 신고 불가
                if (hostParticipant.getUser().getId().equals(reporter.getId())) {
                    log.warn("🚫 [신고 제한] 사용자가 자신의 챌린지 (ID: {})을 신고하려고 했습니다.", challenge.getChallengeId());
                    throw new RuntimeException("자신이 생성한 챌린지는 신고할 수 없습니다.");
                }

                log.info("📝 [챌린지 신고] 신고 대상: {} (챌린지 ID: {})", hostParticipant.getUser().getEmail(), challenge.getChallengeId());

                reportEntity.setReportedChallenge(challenge);
                reportEntity.setReportedUser(hostParticipant.getUser());
                break;

            case USER:
                UserEntity reportedUser = userRepository.findById(reportRequestDto.getTargetId())
                        .orElseThrow(() -> {
                            log.error("❌ [신고 오류] 신고할 사용자 (ID: {})를 찾을 수 없습니다.", reportRequestDto.getTargetId());
                            return new RuntimeException("신고할 사용자를 찾을 수 없습니다.");
                        });

                // 자기 자신 신고 불가
                if (reportedUser.getId().equals(reporter.getId())) {
                    log.warn("🚫 [신고 제한] 사용자가 자기 자신을 신고하려고 했습니다.");
                    throw new RuntimeException("자신을 신고할 수 없습니다.");
                }

                log.info("📝 [사용자 신고] 신고 대상: {} (사용자 ID: {})", reportedUser.getEmail(), reportedUser.getId());

                reportEntity.setReportedUser(reportedUser);
                break;

            default:
                log.error("❌ [신고 오류] 지원하지 않는 신고 타입: {}", reportType);
                throw new RuntimeException("지원하지 않는 신고 타입입니다.");
        }

        // 신고 저장
        ReportEntity savedReport = reportRepository.save(reportEntity);
        log.info("✅ [신고 완료] 신고 ID: {}, 신고자: {}, 대상: {}",
                savedReport.getReportId(), reporter.getEmail(), reportRequestDto.getTargetId());

        return ReportResponseDto.builder()
                .reportId(savedReport.getReportId())
                .message("신고가 접수되었습니다.")
                .build();
    }

    @Transactional
    public void confirmReport(Long reportId) {
        // 신고 내역 조회
        ReportEntity report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("신고 내역을 찾을 수 없습니다. ID: " + reportId));

        log.info("✅ [신고 확인] 신고 ID: {} / 신고 타입: {}", report.getReportId(), report.getReportType());

        // 신고 타입에 따라 대상 엔티티 삭제
        switch (report.getReportType()) {
            case POST:
                BoardEntity board = report.getReportedPost();
                if (board != null) {
                    boardRepository.delete(board);
                    log.info("✅ [게시글 삭제] 게시글 ID: {}", board.getPostId());
                }
                break;
            case COMMENT:
                CommentEntity comment = report.getReportedComment();
                if (comment != null) {
                    commentRepository.delete(comment);
                    log.info("✅ [댓글 삭제] 댓글 ID: {}", comment.getCommentId());
                }
                break;
            case CHALLENGE:
                ChallengeEntity challenge = report.getReportedChallenge();
                if (challenge != null) {
                    challengeRepository.delete(challenge);
                    log.info("✅ [챌린지 삭제] 챌린지 ID: {}", challenge.getChallengeId());
                }
                break;
            case USER:
                UserEntity user = report.getReportedUser();
                if (user != null) {
                    userRepository.delete(user);
                    log.info("✅ [유저 삭제] 유저 이메일: {}", user.getEmail());
                }
                break;
            default:
                throw new RuntimeException("지원하지 않는 신고 타입입니다.");
        }

        // 신고 처리 후 신고 내역 삭제
        reportRepository.delete(report);
        log.info("✅ [신고 완료] 신고 내역 삭제 완료. 신고 ID: {}", reportId);
    }

    @Transactional
    public void cancelReport(Long reportId) {
        log.info("🔎 [신고 취소 진행] reportId: {}", reportId);

        ReportEntity report = reportRepository.findById(reportId)
                .orElseThrow(() -> {
                    log.error("❌ [신고 취소 오류] 신고 내역을 찾을 수 없음 - reportId: {}", reportId);
                    return new RuntimeException("신고 내역을 찾을 수 없습니다.");
                });

        // 신고 내역 삭제
        reportRepository.delete(report);
        log.info("✅ [신고 취소 완료] 신고 내역 삭제 - reportId: {}", reportId);
    }



    @Transactional(readOnly = true)
    public ReportDetailResponseDto getReportById(Long reportId) {
        ReportEntity report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("신고 내역을 찾을 수 없습니다. ID: " + reportId));

        return ReportDetailResponseDto.builder()
                .reportId(report.getReportId())
                .reportType(report.getReportType())
                .reportedUser(report.getReportedUser().getEmail())
                .reporter(report.getReporter().getEmail())
                .reason(report.getReason())
                .createdAt(report.getCreatedAt())
                .postId(report.getReportedPost() != null ? report.getReportedPost().getPostId() : null)
                .commentId(report.getReportedComment() != null ? report.getReportedComment().getCommentId() : null)
                .challengeId(report.getReportedChallenge() != null ? report.getReportedChallenge().getChallengeId() : null)
                .build();
    }

    //관리자용 조회
    @Transactional(readOnly = true)
    public List<ReportListResponseDto> getAllReports() {
        List<ReportEntity> reports = reportRepository.findAll();

        return reports.stream()
                .map(report -> new ReportListResponseDto(
                        report.getReportId(),
                        report.getReportType(),
                        report.getReportedUser().getEmail(),
                        report.getReporter().getEmail(),
                        report.getCreatedAt(),
                        report.getReason()
                ))
                .collect(Collectors.toList());
    }
}
