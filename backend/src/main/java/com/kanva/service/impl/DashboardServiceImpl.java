package com.kanva.service.impl;

import com.kanva.domain.task.Task;
import com.kanva.domain.task.TaskRepository;
import com.kanva.domain.task.TaskStatus;
import com.kanva.dto.dashboard.DashboardResponse;
import com.kanva.dto.dashboard.DashboardResponse.DailyStat;
import com.kanva.dto.dashboard.DashboardResponse.DashboardStats;
import com.kanva.dto.dashboard.DashboardResponse.TaskSummary;
import com.kanva.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private static final int MAX_OVERDUE_TASKS = 10;
    private static final int MAX_DUE_SOON_TASKS = 10;
    private static final int DUE_SOON_DAYS = 7;

    private final TaskRepository taskRepository;

    @Override
    public DashboardResponse getDashboard(Long userId, YearMonth month) {
        LocalDate startDate = month.atDay(1);
        LocalDate endDate = month.atEndOfMonth();
        LocalDate today = LocalDate.now();

        // 1. 월 범위 전체 Task 조회 (1회 쿼리)
        List<Task> monthTasks = taskRepository.findByUserIdAndDateRange(userId, startDate, endDate);

        // 2. dailyStats 계산
        List<DailyStat> dailyStats = buildDailyStats(monthTasks, startDate, endDate);

        // 3. stats 계산 (월 전체)
        DashboardStats stats = buildStats(monthTasks, today);

        // 4. overdueTasks 계산 (미완료 & dueDate < today)
        List<TaskSummary> overdueTasks = buildOverdueTasks(monthTasks, today);

        // 5. dueSoonTasks (today <= dueDate <= today+7 && not completed)
        List<TaskSummary> dueSoonTasks = buildDueSoonTasks(userId, today);

        return DashboardResponse.builder()
                .stats(stats)
                .dailyStats(dailyStats)
                .overdueTasks(overdueTasks)
                .dueSoonTasks(dueSoonTasks)
                .build();
    }

    private List<DailyStat> buildDailyStats(List<Task> tasks, LocalDate startDate, LocalDate endDate) {
        // 날짜별로 그룹핑
        Map<LocalDate, List<Task>> tasksByDate = tasks.stream()
                .collect(Collectors.groupingBy(t -> t.getDailyNote().getDate()));

        List<DailyStat> dailyStats = new ArrayList<>();
        LocalDate current = startDate;

        while (!current.isAfter(endDate)) {
            List<Task> dayTasks = tasksByDate.getOrDefault(current, Collections.emptyList());
            int totalCount = dayTasks.size();
            int completedCount = (int) dayTasks.stream()
                    .filter(t -> t.getStatus() == TaskStatus.COMPLETED)
                    .count();

            dailyStats.add(DailyStat.builder()
                    .date(current.toString())
                    .totalCount(totalCount)
                    .completedCount(completedCount)
                    .build());

            current = current.plusDays(1);
        }

        return dailyStats;
    }

    private DashboardStats buildStats(List<Task> tasks, LocalDate today) {
        int completed = 0;
        int inProgress = 0;
        int pending = 0;
        int overdue = 0;

        for (Task task : tasks) {
            switch (task.getStatus()) {
                case COMPLETED -> completed++;
                case IN_PROGRESS -> inProgress++;
                case PENDING -> pending++;
            }

            // overdue: 미완료 & dueDate < today
            if (task.getDueDate() != null
                    && task.getDueDate().isBefore(today)
                    && task.getStatus() != TaskStatus.COMPLETED) {
                overdue++;
            }
        }

        return DashboardStats.builder()
                .completed(completed)
                .inProgress(inProgress)
                .pending(pending)
                .overdue(overdue)
                .build();
    }

    private List<TaskSummary> buildOverdueTasks(List<Task> tasks, LocalDate today) {
        return tasks.stream()
                .filter(t -> t.getDueDate() != null
                        && t.getDueDate().isBefore(today)
                        && t.getStatus() != TaskStatus.COMPLETED)
                .sorted(Comparator.comparing(Task::getDueDate).reversed())
                .limit(MAX_OVERDUE_TASKS)
                .map(this::toTaskSummary)
                .collect(Collectors.toList());
    }

    private List<TaskSummary> buildDueSoonTasks(Long userId, LocalDate today) {
        LocalDate endDate = today.plusDays(DUE_SOON_DAYS);
        List<Task> dueSoonTasks = taskRepository.findDueSoonTasks(userId, today, endDate);

        return dueSoonTasks.stream()
                .limit(MAX_DUE_SOON_TASKS)
                .map(this::toTaskSummary)
                .collect(Collectors.toList());
    }

    private TaskSummary toTaskSummary(Task task) {
        return TaskSummary.builder()
                .id(task.getId())
                .title(task.getTitle())
                .date(task.getDailyNote().getDate().toString())
                .dueDate(task.getDueDate() != null ? task.getDueDate().toString() : null)
                .status(task.getStatus().name())
                .build();
    }
}
