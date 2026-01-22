package com.kanva.domain.taskseries;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TaskSeriesRepository extends JpaRepository<TaskSeries, Long> {

    /**
     * 스케줄러용: 특정 날짜에 인스턴스를 생성해야 하는 시리즈 조회
     *
     * 조건:
     * - startDate <= date <= endDate (기본 범위)
     * - COMPLETE_STOPS_SERIES인 경우: stopDate IS NULL OR date <= stopDate
     * - PER_OCCURRENCE인 경우: stopDate 무시
     */
    @Query("""
            SELECT ts FROM TaskSeries ts
            WHERE ts.startDate <= :date
            AND ts.endDate >= :date
            AND (
                ts.completionPolicy = com.kanva.domain.taskseries.CompletionPolicy.PER_OCCURRENCE
                OR (ts.completionPolicy = com.kanva.domain.taskseries.CompletionPolicy.COMPLETE_STOPS_SERIES AND (ts.stopDate IS NULL OR ts.stopDate >= :date))
            )
            """)
    List<TaskSeries> findGeneratableSeriesForDate(@Param("date") LocalDate date);

    /**
     * 온디맨드 생성용: 특정 사용자의 특정 날짜에 대한 생성 가능 시리즈 조회
     */
    @Query("""
            SELECT ts FROM TaskSeries ts
            WHERE ts.user.id = :userId
            AND ts.startDate <= :date
            AND ts.endDate >= :date
            AND (
                ts.completionPolicy = com.kanva.domain.taskseries.CompletionPolicy.PER_OCCURRENCE
                OR (ts.completionPolicy = com.kanva.domain.taskseries.CompletionPolicy.COMPLETE_STOPS_SERIES AND (ts.stopDate IS NULL OR ts.stopDate >= :date))
            )
            """)
    List<TaskSeries> findGeneratableSeriesForUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    /**
     * 사용자의 시리즈 목록 조회 (전체)
     */
    List<TaskSeries> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 사용자의 활성 시리즈 목록 조회
     * - PER_OCCURRENCE: 항상 활성
     * - COMPLETE_STOPS_SERIES: stopDate가 null인 경우에만 활성
     */
    @Query("""
            SELECT ts FROM TaskSeries ts
            WHERE ts.user.id = :userId
            AND (
                ts.completionPolicy = com.kanva.domain.taskseries.CompletionPolicy.PER_OCCURRENCE
                OR (ts.completionPolicy = com.kanva.domain.taskseries.CompletionPolicy.COMPLETE_STOPS_SERIES AND ts.stopDate IS NULL)
            )
            ORDER BY ts.createdAt DESC
            """)
    List<TaskSeries> findActiveSeriesByUserId(@Param("userId") Long userId);
}
