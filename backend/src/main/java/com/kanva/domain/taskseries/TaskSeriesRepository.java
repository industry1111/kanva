package com.kanva.domain.taskseries;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TaskSeriesRepository extends JpaRepository<TaskSeries, Long> {

    /**
     * 스케줄러용: 오늘 인스턴스를 생성해야 하는 ACTIVE 시리즈 조회
     * - status = ACTIVE
     * - today가 startDate ~ endDate 범위 내
     * - stopDate가 null이거나 today <= stopDate
     */
    @Query("""
            SELECT ts FROM TaskSeries ts
            WHERE ts.status = 'ACTIVE'
            AND ts.startDate <= :today
            AND ts.endDate >= :today
            AND (ts.stopDate IS NULL OR ts.stopDate >= :today)
            """)
    List<TaskSeries> findActiveSeriesForDate(@Param("today") LocalDate today);

    /**
     * 사용자의 시리즈 목록 조회
     */
    List<TaskSeries> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 사용자의 ACTIVE 시리즈 목록 조회
     */
    List<TaskSeries> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, TaskSeriesStatus status);
}
