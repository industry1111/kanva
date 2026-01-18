package com.kanva.domain.dailynote;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyNoteRepository extends JpaRepository<DailyNote, Long> {

    Optional<DailyNote> findByUserIdAndDate(Long userId, LocalDate date);

    List<DailyNote> findByUserId(Long userId);

    @Query("""
            SELECT d FROM DailyNote d
            WHERE d.userId = :userId
            AND d.date BETWEEN :startDate AND :endDate
            ORDER BY d.date DESC
            """)
    List<DailyNote> findByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    boolean existsByUserIdAndDate(Long userId, LocalDate date);
}
