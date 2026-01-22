package com.kanva.service.impl;

import com.kanva.domain.dailynote.DailyNote;
import com.kanva.domain.dailynote.DailyNoteRepository;
import com.kanva.domain.task.Task;
import com.kanva.domain.task.TaskRepository;
import com.kanva.domain.task.TaskStatus;
import com.kanva.domain.taskseries.TaskSeries;
import com.kanva.domain.user.User;
import com.kanva.domain.user.UserRepository;
import com.kanva.dto.task.TaskPositionUpdateRequest;
import com.kanva.dto.task.TaskRequest;
import com.kanva.dto.task.TaskResponse;
import com.kanva.dto.task.TaskStatusUpdateRequest;
import com.kanva.exception.TaskNotFoundException;
import com.kanva.exception.UserNotFoundException;
import com.kanva.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final DailyNoteRepository dailyNoteRepository;
    private final UserRepository userRepository;

    @Override
    public List<TaskResponse> getTasksByDate(Long userId, LocalDate date) {
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
                .dueDate(request.getDueDate())
                .status(request.getStatus())
                .position(newPosition)
                .build();

        Task savedTask = taskRepository.save(task);
        return TaskResponse.from(savedTask);
    }

    @Override
    @Transactional
    public TaskResponse updateTask(Long userId, Long taskId, TaskRequest request) {
        Task task = findTaskByIdAndUserId(taskId, userId);

        task.updateTitle(request.getTitle());
        task.updateDescription(request.getDescription());
        task.updateDueDate(request.getDueDate());

        if (request.getStatus() != null) {
            TaskStatus oldStatus = task.getStatus();
            task.updateStatus(request.getStatus());

            // 시리즈 Task가 COMPLETED로 변경되면 시리즈 중단
            if (request.getStatus() == TaskStatus.COMPLETED && oldStatus != TaskStatus.COMPLETED) {
                handleSeriesCompletion(task);
            }
        }

        return TaskResponse.from(task);
    }

    @Override
    @Transactional
    public TaskResponse updateTaskStatus(Long userId, Long taskId, TaskStatusUpdateRequest request) {
        Task task = findTaskByIdAndUserId(taskId, userId);
        TaskStatus oldStatus = task.getStatus();

        task.updateStatus(request.getStatus());

        // 시리즈 Task가 COMPLETED로 변경되면 시리즈 중단
        if (request.getStatus() == TaskStatus.COMPLETED && oldStatus != TaskStatus.COMPLETED) {
            handleSeriesCompletion(task);
        }

        return TaskResponse.from(task);
    }

    @Override
    @Transactional
    public TaskResponse toggleTask(Long userId, Long taskId) {
        Task task = findTaskByIdAndUserId(taskId, userId);
        boolean wasCompleted = task.isCompleted();

        task.toggle();

        // 시리즈 Task가 COMPLETED로 토글되면 시리즈 중단
        if (!wasCompleted && task.isCompleted()) {
            handleSeriesCompletion(task);
        }

        return TaskResponse.from(task);
    }

    @Override
    @Transactional
    public void deleteTask(Long userId, Long taskId) {
        Task task = findTaskByIdAndUserId(taskId, userId);
        taskRepository.delete(task);
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
        return taskRepository.findOverdueTasks(userId, LocalDate.now())
                .stream()
                .map(TaskResponse::from)
                .toList();
    }

    /**
     * 시리즈 Task 완료 시 시리즈 중단 처리
     */
    private void handleSeriesCompletion(Task task) {
        if (!task.isSeriesTask()) {
            return;
        }

        TaskSeries series = task.getSeries();
        if (series != null && series.isActive()) {
            LocalDate taskDate = task.getDailyNote().getDate();
            series.stop(taskDate);
            log.info("Series {} stopped due to task {} completion on date {}",
                    series.getId(), task.getId(), taskDate);
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
