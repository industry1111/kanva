package com.kanva.service.impl;

import com.kanva.domain.dailynote.DailyNote;
import com.kanva.domain.dailynote.DailyNoteRepository;
import com.kanva.domain.task.Task;
import com.kanva.domain.task.TaskRepository;
import com.kanva.domain.task.TaskStatus;
import com.kanva.domain.taskseries.CompletionPolicy;
import com.kanva.domain.user.User;
import com.kanva.domain.user.UserRepository;
import com.kanva.dto.task.TaskPositionUpdateRequest;
import com.kanva.dto.task.TaskRequest;
import com.kanva.dto.task.TaskResponse;
import com.kanva.dto.task.TaskStatusUpdateRequest;
import com.kanva.exception.TaskNotFoundException;
import com.kanva.exception.TaskStatusChangeNotAllowedException;
import com.kanva.exception.UserNotFoundException;
import com.kanva.service.TaskSeriesService;
import com.kanva.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * Task 비즈니스 로직
 *
 * 시간 기준: 모든 날짜 판단은 Seoul Clock 기준
 * 미래 Task: 상태 변경 불가, 삭제/수정은 가능
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final DailyNoteRepository dailyNoteRepository;
    private final UserRepository userRepository;
    private final TaskSeriesService taskSeriesService;
    private final Clock clock;

    @Override
    @Transactional
    public List<TaskResponse> getTasksByDate(Long userId, LocalDate date) {
        // 해당 날짜에 대한 시리즈 Task 온디맨드 생성
        taskSeriesService.generateTasksForDate(userId, date);

        return taskRepository.findByUserIdAndDate(userId, date)
                .stream()
                .map(TaskResponse::from)
                .toList();
    }

    @Override
    public TaskResponse getTask(Long userId, Long taskId) {
        Task task = findTaskByIdAndUserId(taskId, userId);
        return TaskResponse.from(task);
    }

    @Override
    @Transactional
    public TaskResponse createTask(Long userId, LocalDate date, TaskRequest request) {
        User user = findUserById(userId);
        DailyNote dailyNote = getOrCreateDailyNote(user, date);

        Integer maxPosition = taskRepository.findMaxPositionByDailyNoteId(dailyNote.getId());
        int newPosition = (maxPosition != null) ? maxPosition + 1 : 0;

        Task task = Task.builder()
                .dailyNote(dailyNote)
                .title(request.getTitle())
                .description(request.getDescription())
                .dueDate(null)  // 단일 Task는 dueDate 없음
                .status(request.getStatus())
                .position(newPosition)
                .build();

        Task savedTask = taskRepository.save(task);

        // repeatDaily=true인 경우 시리즈 생성
        if (request.isRepeatDaily() && request.getEndDate() != null) {
            CompletionPolicy policy = request.isStopOnComplete()
                    ? CompletionPolicy.COMPLETE_STOPS_SERIES
                    : CompletionPolicy.PER_OCCURRENCE;
            taskSeriesService.createSeriesFromTask(savedTask, request.getEndDate(), policy);
        }

        return TaskResponse.from(savedTask);
    }

    @Override
    @Transactional
    public TaskResponse updateTask(Long userId, Long taskId, TaskRequest request) {
        Task task = findTaskByIdAndUserId(taskId, userId);

        task.updateTitle(request.getTitle());
        task.updateDescription(request.getDescription());

        // repeatDaily=true이고 시리즈가 아직 없는 경우 시리즈 생성
        if (request.isRepeatDaily() && request.getEndDate() != null && !task.isSeriesTask()) {
            CompletionPolicy policy = request.isStopOnComplete()
                    ? CompletionPolicy.COMPLETE_STOPS_SERIES
                    : CompletionPolicy.PER_OCCURRENCE;
            taskSeriesService.createSeriesFromTask(task, request.getEndDate(), policy);
        }

        // 상태 변경 처리
        if (request.getStatus() != null) {
            validateNotFutureTask(task);
            TaskStatus oldStatus = task.getStatus();
            task.updateStatus(request.getStatus());

            // PENDING -> COMPLETED 전환 시 시리즈 완료 처리
            if (request.getStatus() == TaskStatus.COMPLETED && oldStatus != TaskStatus.COMPLETED) {
                taskSeriesService.handleTaskCompletion(task);
            }
        }

        return TaskResponse.from(task);
    }

    @Override
    @Transactional
    public TaskResponse updateTaskStatus(Long userId, Long taskId, TaskStatusUpdateRequest request) {
        Task task = findTaskByIdAndUserId(taskId, userId);
        validateNotFutureTask(task);

        TaskStatus oldStatus = task.getStatus();
        task.updateStatus(request.getStatus());

        // PENDING -> COMPLETED 전환 시 시리즈 완료 처리
        if (request.getStatus() == TaskStatus.COMPLETED && oldStatus != TaskStatus.COMPLETED) {
            taskSeriesService.handleTaskCompletion(task);
        }

        return TaskResponse.from(task);
    }

    @Override
    @Transactional
    public TaskResponse toggleTask(Long userId, Long taskId) {
        Task task = findTaskByIdAndUserId(taskId, userId);
        validateNotFutureTask(task);

        boolean wasCompleted = task.isCompleted();
        task.toggle();

        // PENDING -> COMPLETED 토글 시 시리즈 완료 처리
        if (!wasCompleted && task.isCompleted()) {
            taskSeriesService.handleTaskCompletion(task);
        }

        return TaskResponse.from(task);
    }

    @Override
    @Transactional
    public void deleteTask(Long userId, Long taskId) {
        Task task = findTaskByIdAndUserId(taskId, userId);
        Long seriesId = task.getSeriesId();
        taskRepository.delete(task);

        // 시리즈 Task인 경우 자동 정리 확인
        if (seriesId != null) {
            taskRepository.flush();
            taskSeriesService.cleanupIfEligible(seriesId);
        }
    }

    @Override
    @Transactional
    public List<TaskResponse> updateTaskPositions(Long userId, LocalDate date, TaskPositionUpdateRequest request) {
        List<Long> taskIds = request.getTaskIds();

        for (int i = 0; i < taskIds.size(); i++) {
            Task task = findTaskByIdAndUserId(taskIds.get(i), userId);
            task.updatePosition(i);
        }

        return taskRepository.findByUserIdAndDate(userId, date)
                .stream()
                .map(TaskResponse::from)
                .toList();
    }

    @Override
    public List<TaskResponse> getOverdueTasks(Long userId) {
        LocalDate today = LocalDate.now(clock);
        return taskRepository.findOverdueTasks(userId, today)
                .stream()
                .map(TaskResponse::from)
                .toList();
    }

    /**
     * 미래 날짜 Task 상태 변경 불가 검증 (Seoul Clock 기준)
     */
    private void validateNotFutureTask(Task task) {
        LocalDate taskDate = task.getDailyNote().getDate();
        LocalDate today = LocalDate.now(clock);
        if (taskDate.isAfter(today)) {
            throw new TaskStatusChangeNotAllowedException();
        }
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private Task findTaskByIdAndUserId(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        // 해당 사용자의 Task인지 확인
        if (!task.getDailyNote().getUser().getId().equals(userId)) {
            throw new TaskNotFoundException(taskId);
        }

        return task;
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
