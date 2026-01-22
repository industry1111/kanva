package com.kanva.dto.dashboard;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DashboardResponse {

    private DashboardStats stats;
    private List<DailyStat> dailyStats;
    private List<TaskSummary> overdueTasks;
    private List<TaskSummary> dueSoonTasks;

    @Getter
    @Builder
    public static class DashboardStats {
        private int completed;
        private int inProgress;
        private int pending;
        private int overdue;
    }

    @Getter
    @Builder
    public static class DailyStat {
        private String date;
        private int totalCount;
        private int completedCount;
    }

    @Getter
    @Builder
    public static class TaskSummary {
        private Long id;
        private String title;
        private String date;
        private String dueDate;
        private String status;
    }
}
