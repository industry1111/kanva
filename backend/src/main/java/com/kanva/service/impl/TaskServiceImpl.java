package com.kanva.service.impl;

import com.kanva.domain.dailynote.DailyNote;
import com.kanva.domain.dailynote.DailyNoteRepository;
import com.kanva.domain.task.Task;
import com.kanva.domain.task.TaskRepository;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
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
            task.updateStatus(request.getStatus());
        }

        return TaskResponse.from(task);
    }

    @Override
    @Transactional
    public TaskResponse updateTaskStatus(Long userId, Long taskId, TaskStatusUpdateRequest request) {
        Task task = findTaskByIdAndUserId(taskId, userId);
        task.updateStatus(request.getStatus());
        return TaskResponse.from(task);
    }

    @Override
    @Transactional
    public TaskResponse toggleTask(Long userId, Long taskId) {
        Task task = findTaskByIdAndUserId(taskId, userId);
        task.toggle();
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
