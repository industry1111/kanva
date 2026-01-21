package com.kanva.domain.dailynote;

import com.kanva.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyNoteRepository extends JpaRepository<DailyNote, Long> {

    Optional<DailyNote> findByUserAndDate(User user, LocalDate date);

    @Query("SELECT d FROM DailyNote d WHERE d.user.id = :userId AND d.date = :date")
    Optional<DailyNote> findByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    List<DailyNote> findByUser(User user);

    @Query("SELECT d FROM DailyNote d WHERE d.user.id = :userId")
    List<DailyNote> findByUserId(@Param("userId") Long userId);

    @Query("""
            SELECT d FROM DailyNote d
            WHERE d.user.id = :userId
            AND d.date BETWEEN :startDate AND :endDate
            ORDER BY d.date DESC
            """)
    List<DailyNote> findByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM DailyNote d WHERE d.user.id = :userId AND d.date = :date")
    boolean existsByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
}
