package com.kanva.domain.report;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface AIReportRepository extends JpaRepository<AIReport, Long> {

    /**
     * 사용자의 최신 완료된 주간 리포트 조회
     */
    @Query("SELECT r FROM AIReport r WHERE r.user.id = :userId AND r.status = 'COMPLETED' " +
            "AND r.periodType = 'WEEKLY' ORDER BY r.periodEnd DESC LIMIT 1")
    Optional<AIReport> findLatestWeeklyReport(@Param("userId") Long userId);

    /**
     * 사용자의 특정 기간에 해당하는 리포트 조회
     */
    @Query("SELECT r FROM AIReport r WHERE r.user.id = :userId " +
            "AND r.periodStart = :periodStart AND r.periodEnd = :periodEnd")
    Optional<AIReport> findByUserIdAndPeriod(
            @Param("userId") Long userId,
            @Param("periodStart") LocalDate periodStart,
            @Param("periodEnd") LocalDate periodEnd);

    /**
     * 사용자 리포트 히스토리 (페이징)
     */
    Page<AIReport> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 사용자 리포트 히스토리 (완료된 것만)
     */
    Page<AIReport> findByUserIdAndStatusOrderByCreatedAtDesc(
            Long userId, ReportStatus status, Pageable pageable);

    /**
     * 사용자의 특정 기간 타입 최신 완료 리포트 (트렌드 비교용)
     */
    @Query("SELECT r FROM AIReport r WHERE r.user.id = :userId " +
            "AND r.periodType = :periodType AND r.status = 'COMPLETED' " +
            "ORDER BY r.createdAt DESC LIMIT 1")
    Optional<AIReport> findLatestCompletedByUserAndType(
            @Param("userId") Long userId,
            @Param("periodType") ReportPeriodType periodType);
}
