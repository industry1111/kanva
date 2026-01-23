package com.kanva.domain.task;

import com.kanva.domain.dailynote.DailyNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByDailyNoteOrderByPositionAsc(DailyNote dailyNote);

    @Query("SELECT t FROM Task t WHERE t.dailyNote.id = :dailyNoteId ORDER BY t.position ASC")
    List<Task> findByDailyNoteIdOrderByPositionAsc(@Param("dailyNoteId") Long dailyNoteId);

    @Query("SELECT t FROM Task t WHERE t.dailyNote.user.id = :userId AND t.dailyNote.date = :date ORDER BY t.position ASC")
    List<Task> findByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Query("SELECT t FROM Task t WHERE t.dailyNote.user.id = :userId AND t.status = :status ORDER BY t.dueDate ASC NULLS LAST")
    List<Task> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") TaskStatus status);

    @Query("SELECT t FROM Task t WHERE t.dailyNote.user.id = :userId AND t.dueDate = :dueDate AND t.status != 'COMPLETED' ORDER BY t.position ASC")
    List<Task> findByUserIdAndDueDateAndNotCompleted(@Param("userId") Long userId, @Param("dueDate") LocalDate dueDate);

    @Query("SELECT t FROM Task t WHERE t.dailyNote.user.id = :userId AND t.dueDate < :today AND t.status != 'COMPLETED' ORDER BY t.dueDate ASC")
    List<Task> findOverdueTasks(@Param("userId") Long userId, @Param("today") LocalDate today);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.dailyNote.id = :dailyNoteId")
    int countByDailyNoteId(@Param("dailyNoteId") Long dailyNoteId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.dailyNote.id = :dailyNoteId AND t.status = 'COMPLETED'")
    int countCompletedByDailyNoteId(@Param("dailyNoteId") Long dailyNoteId);

    @Query("SELECT MAX(t.position) FROM Task t WHERE t.dailyNote.id = :dailyNoteId")
    Integer findMaxPositionByDailyNoteId(@Param("dailyNoteId") Long dailyNoteId);

    // Dashboard: 월 범위 조회
    @Query("SELECT t FROM Task t JOIN FETCH t.dailyNote d WHERE d.user.id = :userId AND d.date BETWEEN :startDate AND :endDate ORDER BY d.date ASC, t.position ASC")
    List<Task> findByUserIdAndDateRange(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Dashboard: 마감 임박 (today ~ today+7, not completed)
    @Query("SELECT t FROM Task t JOIN FETCH t.dailyNote d WHERE d.user.id = :userId AND t.dueDate BETWEEN :startDate AND :endDate AND t.status != 'COMPLETED' ORDER BY t.dueDate ASC")
    List<Task> findDueSoonTasks(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // TaskSeries: 해당 시리즈의 해당 날짜 인스턴스 존재 여부 확인
    boolean existsBySeries_IdAndTaskDate(Long seriesId, LocalDate taskDate);

    /**
     * 시리즈의 특정 날짜 이후 인스턴스 일괄 삭제
     * COMPLETE_STOPS_SERIES 정책에서 완료 시 미래 인스턴스 정리용
     *
     * @param seriesId 시리즈 ID
     * @param cutoffDate 기준 날짜 (이 날짜 초과인 인스턴스 삭제)
     * @return 삭제된 행 수
     */
    @Modifying
    @Query("DELETE FROM Task t WHERE t.series.id = :seriesId AND t.taskDate > :cutoffDate")
    int deleteBySeries_IdAndTaskDateAfter(@Param("seriesId") Long seriesId, @Param("cutoffDate") LocalDate cutoffDate);
}
