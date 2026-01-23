package com.kanva.domain.taskseries;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface TaskSeriesExcludedDateRepository extends JpaRepository<TaskSeriesExcludedDate, TaskSeriesExcludedDateId> {

    boolean existsByIdTaskSeriesIdAndIdDate(Long taskSeriesId, LocalDate date);

    @Modifying
    @Query("DELETE FROM TaskSeriesExcludedDate e WHERE e.id.taskSeriesId = :taskSeriesId AND e.id.date = :date")
    void deleteByTaskSeriesIdAndDate(@Param("taskSeriesId") Long taskSeriesId, @Param("date") LocalDate date);

    long countByIdTaskSeriesId(Long taskSeriesId);

    @Modifying
    @Query("DELETE FROM TaskSeriesExcludedDate e WHERE e.id.taskSeriesId = :taskSeriesId")
    void deleteAllByTaskSeriesId(@Param("taskSeriesId") Long taskSeriesId);
}
