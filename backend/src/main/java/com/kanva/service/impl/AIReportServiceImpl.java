package com.kanva.service.impl;

import com.kanva.domain.report.*;
import com.kanva.domain.task.Task;
import com.kanva.domain.task.TaskRepository;
import com.kanva.domain.user.User;
import com.kanva.domain.user.UserRepository;
import com.kanva.dto.report.AIReportDetailResponse;
import com.kanva.dto.report.AIReportResponse;
import com.kanva.dto.report.AIReportSummaryResponse;
import com.kanva.exception.ReportGenerationException;
import com.kanva.exception.ReportNotFoundException;
import com.kanva.exception.UserNotFoundException;
import com.kanva.service.AIReportService;
import com.kanva.service.report.AIAnalysisService;
import com.kanva.service.report.GeminiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
public class AIReportServiceImpl implements AIReportService {

    private final AIReportRepository aiReportRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final AIAnalysisService geminiAnalysisService;
    private final AIAnalysisService mockAnalysisService;
    private final GeminiClient geminiClient;
    private final Clock clock;

    public AIReportServiceImpl(
            AIReportRepository aiReportRepository,
            TaskRepository taskRepository,
            UserRepository userRepository,
            @Qualifier("geminiAIAnalysisService") AIAnalysisService geminiAnalysisService,
            @Qualifier("mockAIAnalysisService") AIAnalysisService mockAnalysisService,
            GeminiClient geminiClient,
            Clock clock) {
        this.aiReportRepository = aiReportRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.geminiAnalysisService = geminiAnalysisService;
        this.mockAnalysisService = mockAnalysisService;
        this.geminiClient = geminiClient;
        this.clock = clock;
    }

    private AIAnalysisService getAnalysisService() {
        if (geminiClient.isAvailable()) {
            log.info("Using Gemini AI Analysis Service");
            return geminiAnalysisService;
        }
        log.info("Using Mock AI Analysis Service (Gemini not configured)");
        return mockAnalysisService;
    }

    @Override
    public AIReportSummaryResponse getWeeklySummary(Long userId) {
        return aiReportRepository.findLatestWeeklyReport(userId)
                .map(AIReportSummaryResponse::from)
                .orElse(AIReportSummaryResponse.empty());
    }

    @Override
    @Transactional
    public AIReportResponse generateReport(Long userId, ReportPeriodType periodType,
                                           LocalDate periodStart, LocalDate periodEnd) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // 기간 계산 (periodType에 따라)
        LocalDate start = periodStart;
        LocalDate end = periodEnd;

        if (periodType == ReportPeriodType.WEEKLY) {
            LocalDate today = LocalDate.now(clock);
            start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            end = start.plusDays(6);
        } else if (periodType == ReportPeriodType.MONTHLY) {
            LocalDate today = LocalDate.now(clock);
            start = today.withDayOfMonth(1);
            end = today.with(TemporalAdjusters.lastDayOfMonth());
        }

        // 기존 리포트가 있으면 반환
        var existingReport = aiReportRepository.findByUserIdAndPeriod(userId, start, end);
        if (existingReport.isPresent() && existingReport.get().isCompleted()) {
            return AIReportResponse.from(existingReport.get());
        }

        // 새 리포트 생성
        AIReport report = AIReport.builder()
                .user(user)
                .periodType(periodType)
                .periodStart(start)
                .periodEnd(end)
                .build();

        aiReportRepository.save(report);

        try {
            // 현재 기간 Task 조회
            List<Task> currentTasks = taskRepository.findByUserIdAndDateRange(userId, start, end);

            // 이전 기간 Task 조회 (비교용)
            LocalDate prevStart = start.minusDays(ChronoUnit.DAYS.between(start, end) + 1);
            LocalDate prevEnd = start.minusDays(1);
            List<Task> previousTasks = taskRepository.findByUserIdAndDateRange(userId, prevStart, prevEnd);

            // AI 분석 수행
            AIAnalysisService.AnalysisResult result = getAnalysisService().analyze(currentTasks, previousTasks);

            // 결과 저장
            report.complete(
                    result.getTotalTasks(),
                    result.getCompletedTasks(),
                    result.getCompletionRate(),
                    result.getTrend(),
                    result.getSummary(),
                    result.getInsights(),
                    result.getRecommendations()
            );

        } catch (Exception e) {
            report.fail(e.getMessage());
            throw new ReportGenerationException(e.getMessage(), e);
        }

        return AIReportResponse.from(report);
    }

    @Override
    public AIReportDetailResponse getReportDetail(Long userId, Long reportId) {
        AIReport report = aiReportRepository.findById(reportId)
                .orElseThrow(() -> new ReportNotFoundException(reportId));

        // 본인 리포트인지 확인
        if (!report.getUser().getId().equals(userId)) {
            throw new ReportNotFoundException(reportId);
        }

        return AIReportDetailResponse.from(report);
    }

    @Override
    public Page<AIReportResponse> getReportHistory(Long userId, Pageable pageable) {
        return aiReportRepository.findByUserIdAndStatusOrderByCreatedAtDesc(
                        userId, ReportStatus.COMPLETED, pageable)
                .map(AIReportResponse::from);
    }

    @Override
    @Transactional
    public void submitFeedback(Long userId, Long reportId, ReportFeedback feedback) {
        AIReport report = aiReportRepository.findById(reportId)
                .orElseThrow(() -> new ReportNotFoundException(reportId));

        // 본인 리포트인지 확인
        if (!report.getUser().getId().equals(userId)) {
            throw new ReportNotFoundException(reportId);
        }

        report.submitFeedback(feedback);
    }
}
