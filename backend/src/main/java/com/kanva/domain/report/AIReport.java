package com.kanva.domain.report;

import com.kanva.domain.BaseEntity;
import com.kanva.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "ai_reports",
        indexes = {
                @Index(name = "idx_ai_report_user_period", columnList = "user_id, period_start, period_end"),
                @Index(name = "idx_ai_report_user_status", columnList = "user_id, status")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AIReport extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportPeriodType periodType;

    @Column(nullable = false)
    private LocalDate periodStart;

    @Column(nullable = false)
    private LocalDate periodEnd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportStatus status;

    // 분석 결과 데이터
    private Integer totalTasks;
    private Integer completedTasks;
    private Integer completionRate;
    private String trend;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String insights;

    @Column(columnDefinition = "TEXT")
    private String recommendations;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ReportFeedback feedback;

    @Builder
    public AIReport(User user, ReportPeriodType periodType, LocalDate periodStart, LocalDate periodEnd) {
        this.user = user;
        this.periodType = periodType;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.status = ReportStatus.GENERATING;
    }

    public void complete(Integer totalTasks, Integer completedTasks, Integer completionRate,
                         String trend, String summary, String insights, String recommendations) {
        this.totalTasks = totalTasks;
        this.completedTasks = completedTasks;
        this.completionRate = completionRate;
        this.trend = trend;
        this.summary = summary;
        this.insights = insights;
        this.recommendations = recommendations;
        this.status = ReportStatus.COMPLETED;
    }

    public void fail(String errorMessage) {
        this.errorMessage = errorMessage;
        this.status = ReportStatus.FAILED;
    }

    public void submitFeedback(ReportFeedback feedback) {
        this.feedback = feedback;
    }

    public boolean isCompleted() {
        return this.status == ReportStatus.COMPLETED;
    }

    public boolean isFailed() {
        return this.status == ReportStatus.FAILED;
    }

    public boolean isGenerating() {
        return this.status == ReportStatus.GENERATING;
    }
}
