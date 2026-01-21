package com.kanva.domain.dailynote;

import com.kanva.domain.BaseEntity;
import com.kanva.domain.task.Task;
import com.kanva.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate date;

    @Column(columnDefinition = "TEXT")
    private String content;

    @OneToMany(mappedBy = "dailyNote", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<Task> tasks = new ArrayList<>();

    @Builder
    public DailyNote(User user, LocalDate date, String content) {
        this.user = user;
        this.date = date;
        this.content = content;
    }
    //내용 변경
    public void updateContent(String content) {
        this.content = content;
    }

    // Task 추가  
    public void addTask(Task task) {
        this.tasks.add(task);
        task.assignToDailyNote(this);
    }

    // Task 제거  
    public void removeTask(Task task) {
        this.tasks.remove(task);
    }

    // userId 조회
    public Long getUserId() {
        return user != null ? user.getId() : null;
    }

    // 완료된 Task 개수
    public int getCompletedTaskCount() {
        return (int) tasks.stream().filter(Task::isCompleted).count();
    }

    // 전체 Task 개수
    public int getTotalTaskCount() {
        return tasks.size();
    }
}
