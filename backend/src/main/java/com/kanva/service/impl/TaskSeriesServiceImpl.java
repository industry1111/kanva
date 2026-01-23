package com.kanva.service.impl;

import com.kanva.domain.dailynote.DailyNote;
import com.kanva.domain.dailynote.DailyNoteRepository;
import com.kanva.domain.task.Task;
import com.kanva.domain.task.TaskRepository;
import com.kanva.domain.task.TaskStatus;
import com.kanva.domain.taskseries.TaskSeries;
import com.kanva.domain.taskseries.TaskSeriesRepository;
import com.kanva.domain.taskseries.TaskSeriesStatus;
import com.kanva.domain.user.User;
import com.kanva.domain.user.UserRepository;
import com.kanva.dto.taskseries.TaskSeriesRequest;
import com.kanva.dto.taskseries.TaskSeriesResponse;
import com.kanva.exception.UserNotFoundException;
import com.kanva.service.TaskSeriesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TaskSeriesServiceImpl implements TaskSeriesService {

    private final TaskSeriesRepository taskSeriesRepository;
    private final TaskRepository taskRepository;
    private final DailyNoteRepository dailyNoteRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public TaskSeriesResponse createSeries(Long userId, TaskSeriesRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : LocalDate.now();

        TaskSeries series = TaskSeries.builder()
                .user(user)
                .title(request.getTitle())
                .description(request.getDescription())
                .startDate(startDate)
                .endDate(request.getEndDate())
                .stopOnComplete(request.getStopOnComplete())
                .build();

        TaskSeries savedSeries = taskSeriesRepository.save(series);

        // 오늘이 시작일 범위 내라면 즉시 오늘 인스턴스 생성
        LocalDate today = LocalDate.now();
        if (savedSeries.canGenerateFor(today)) {
            createTaskInstance(savedSeries, today);
        }

        return TaskSeriesResponse.from(savedSeries);
    }

    @Override
    public List<TaskSeriesResponse> getUserSeries(Long userId) {
        return taskSeriesRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(TaskSeriesResponse::from)
                .toList();
    }

    @Override
    public List<TaskSeriesResponse> getUserActiveSeries(Long userId) {
        return taskSeriesRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, TaskSeriesStatus.ACTIVE)
                .stream()
                .map(TaskSeriesResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public void generateTodayTasks() {
        LocalDate today = LocalDate.now();
        log.info("Starting daily task generation for date: {}", today);

        List<TaskSeries> activeSeriesList = taskSeriesRepository.findActiveSeriesForDate(today);
        log.info("Found {} active series for today", activeSeriesList.size());

        int createdCount = 0;
        int skippedCount = 0;

        for (TaskSeries series : activeSeriesList) {
            // 멱등성: 이미 해당 날짜에 인스턴스가 존재하는지 확인
            if (taskRepository.existsBySeries_IdAndTaskDate(series.getId(), today)) {
                log.debug("Task already exists for series {} on {}", series.getId(), today);
                skippedCount++;
                continue;
            }

            // 추가 확인: canGenerateFor (stopDate 체크 포함)
            if (!series.canGenerateFor(today)) {
                log.debug("Series {} cannot generate for {}", series.getId(), today);
                skippedCount++;
                continue;
            }

            createTaskInstance(series, today);
            createdCount++;
        }

        log.info("Daily task generation completed. Created: {}, Skipped: {}", createdCount, skippedCount);
    }

    private void createTaskInstance(TaskSeries series, LocalDate date) {
        User user = series.getUser();
        DailyNote dailyNote = getOrCreateDailyNote(user, date);

        Integer maxPosition = taskRepository.findMaxPositionByDailyNoteId(dailyNote.getId());
        int newPosition = (maxPosition != null) ? maxPosition + 1 : 0;

        Task task = Task.builder()
                .dailyNote(dailyNote)
                .series(series)
                .title(series.getTitle())
                .description(series.getDescription())
                .dueDate(series.getEndDate())
                .status(TaskStatus.PENDING)
                .position(newPosition)
                .build();

        taskRepository.save(task);
        log.debug("Created task instance for series {} on date {}", series.getId(), date);
    }

    private DailyNote getOrCreateDailyNote(User user, LocalDate date) {
        return dailyNoteRepository.findByUserAndDate(user, date)
                .orElseGet(() -> {
                    DailyNote newDailyNote = DailyNote.builder()
                            .user(user)
                            .date(date)
                            .content(null)
                            .build();
                    return dailyNoteRepository.save(newDailyNote);
                });
    }
}
