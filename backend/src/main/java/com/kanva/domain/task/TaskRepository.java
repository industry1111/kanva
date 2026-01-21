package com.kanva.domain.task;

import com.kanva.domain.dailynote.DailyNote;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
