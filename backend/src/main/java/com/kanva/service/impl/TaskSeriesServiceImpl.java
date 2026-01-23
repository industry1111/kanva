package com.kanva.service.impl;

import com.kanva.domain.dailynote.DailyNote;
import com.kanva.domain.dailynote.DailyNoteRepository;
import com.kanva.domain.task.Task;
import com.kanva.domain.task.TaskRepository;
import com.kanva.domain.task.TaskStatus;
import com.kanva.domain.taskseries.CompletionPolicy;
import com.kanva.domain.taskseries.TaskSeries;
import com.kanva.domain.taskseries.TaskSeriesRepository;
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

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * TaskSeries 비즈니스 로직
 *
 * 생성 정책:
 * - 스케줄러: 매일 08:00 KST, 오늘 날짜만 생성
 * - 온디맨드: 사용자가 조회 시 해당 날짜 생성 (미래 포함)
 *
 * 완료 정책:
 * - PER_OCCURRENCE: 인스턴스별 완료, 시리즈 계속
 * - COMPLETE_STOPS_SERIES: 완료 시 stopDate 설정, 미래 인스턴스 삭제
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TaskSeriesServiceImpl implements TaskSeriesService {

    private final TaskSeriesRepository taskSeriesRepository;
    private final TaskRepository taskRepository;
    private final DailyNoteRepository dailyNoteRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    @Override
    @Transactional
    public TaskSeriesResponse createSeries(Long userId, TaskSeriesRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        LocalDate today = LocalDate.now(clock);
        LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : today;

        TaskSeries series = TaskSeries.builder()
                .user(user)
                .title(request.getTitle())
                .description(request.getDescription())
                .startDate(startDate)
                .endDate(request.getEndDate())
                .completionPolicy(request.getCompletionPolicy())
                .build();

        TaskSeries savedSeries = taskSeriesRepository.save(series);

        // 오늘이 시작일 범위 내라면 즉시 오늘 인스턴스 생성
        if (savedSeries.canGenerateFor(today)) {
            createTaskInstance(savedSeries, today);
        }

        return TaskSeriesResponse.from(savedSeries);
    }

    @Override
    @Transactional
    public TaskSeries createSeriesFromTask(Task task, LocalDate endDate, CompletionPolicy completionPolicy) {
        User user = task.getDailyNote().getUser();
        LocalDate startDate = task.getDailyNote().getDate();

        TaskSeries series = TaskSeries.builder()
                .user(user)
                .title(task.getTitle())
                .description(task.getDescription())
                .startDate(startDate)
                .endDate(endDate)
                .completionPolicy(completionPolicy)
                .build();

        TaskSeries savedSeries = taskSeriesRepository.save(series);

        // 현재 Task를 시리즈에 연결
        task.assignToSeries(savedSeries);

        log.info("Created series {} from task {} with endDate {} and policy {}",
                savedSeries.getId(), task.getId(), endDate, completionPolicy);
        return savedSeries;
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
        return taskSeriesRepository.findActiveSeriesByUserId(userId)
                .stream()
                .map(TaskSeriesResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public void generateTasksForDate(Long userId, LocalDate date) {
        log.debug("Generating series tasks for user {} on date {}", userId, date);

        List<TaskSeries> seriesList = taskSeriesRepository.findGeneratableSeriesForUserAndDate(userId, date);

        for (TaskSeries series : seriesList) {
            // 이미 해당 날짜에 인스턴스가 존재하는지 확인
            if (taskRepository.existsBySeries_IdAndTaskDate(series.getId(), date)) {
                continue;
            }

            // Entity에서 한번 더 확인
            if (!series.canGenerateFor(date)) {
                continue;
            }

            createTaskInstance(series, date);
            log.debug("Created series task for series {} on date {}", series.getId(), date);
        }
    }

    @Override
    @Transactional
    public void generateTodayTasks() {
        LocalDate today = LocalDate.now(clock);
        log.info("Starting daily task generation for date: {}", today);

        List<TaskSeries> seriesList = taskSeriesRepository.findGeneratableSeriesForDate(today);
        log.info("Found {} generatable series for today", seriesList.size());

        int createdCount = 0;
        int skippedCount = 0;

        for (TaskSeries series : seriesList) {
            // 이미 해당 날짜에 인스턴스가 존재하는지 확인
            if (taskRepository.existsBySeries_IdAndTaskDate(series.getId(), today)) {
                log.debug("Task already exists for series {} on {}", series.getId(), today);
                skippedCount++;
                continue;
            }

            // Entity에서 한번 더 확인
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

    @Override
    @Transactional
    public int handleTaskCompletion(Task task) {
        if (!task.isSeriesTask()) {
            return 0;
        }

        TaskSeries series = task.getSeries();
        if (series == null) {
            return 0;
        }

        // PER_OCCURRENCE 정책은 아무 작업 없음
        if (series.getCompletionPolicy() == CompletionPolicy.PER_OCCURRENCE) {
            log.debug("Series {} has PER_OCCURRENCE policy, no action needed", series.getId());
            return 0;
        }

        // COMPLETE_STOPS_SERIES 정책: stopDate 설정 + 미래 인스턴스 삭제
        LocalDate taskDate = task.getDailyNote().getDate();
        boolean stopped = series.stop(taskDate);

        if (stopped) {
            // 미래 인스턴스 삭제 (taskDate 이후)
            int deletedCount = taskRepository.deleteBySeries_IdAndTaskDateAfter(series.getId(), taskDate);
            log.info("Series {} stopped on {}. Deleted {} future instances",
                    series.getId(), taskDate, deletedCount);
            return deletedCount;
        }

        return 0;
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
                .dueDate(null)  // 시리즈 Task는 개별 dueDate 없음
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
