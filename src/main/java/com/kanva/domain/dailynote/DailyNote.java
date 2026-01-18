package com.kanva.domain.dailynote;

import com.kanva.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "daily_notes",
        indexes = @Index(name = "idx_user_date", columnList = "user_id, date"),
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "date"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyNote extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Builder
    public DailyNote(Long userId, LocalDate date, String content) {
        this.userId = userId;
        this.date = date;
        this.content = content;
    }

    public void updateContent(String content) {
        this.content = content;
    }
}
