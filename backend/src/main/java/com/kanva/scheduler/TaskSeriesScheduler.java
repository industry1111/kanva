package com.kanva.scheduler;

import com.kanva.domain.task.TaskStatus;
import com.kanva.dto.task.TaskResponse;
import com.kanva.service.SlackService;
import com.kanva.service.TaskSeriesService;
import com.kanva.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TaskSeriesScheduler {

    private final TaskSeriesService taskSeriesService;
    private final TaskService taskService;
    private final SlackService slackService;
    private final Clock clock;

    // TODO: 다중 사용자 지원 시 변경 필요
    private static final Long DEFAULT_USER_ID = 1L;

    /**
     * 매일 오전 8시 (Asia/Seoul) 실행
     * 1. 오늘 날짜에 대해 ACTIVE 시리즈의 Task 인스턴스 생성
     * 2. Slack DM으로 오늘의 할 일 알림 전송
     */
    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Seoul")
    public void generateDailyTasks() {
        log.info("TaskSeriesScheduler: Starting daily task generation");
        try {
            taskSeriesService.generateTodayTasks();
            log.info("TaskSeriesScheduler: Daily task generation completed");

            LocalDate today = LocalDate.now(clock);
            List<String> taskTitles = taskService.getTasksByDate(DEFAULT_USER_ID, today)
                    .stream()
                    .map(TaskResponse::getTitle)
                    .toList();

            slackService.sendDailyTaskNotification(today, taskTitles);
            log.info("TaskSeriesScheduler: Morning notification sent");

        } catch (Exception e) {
            log.error("TaskSeriesScheduler: Error during daily task generation", e);
        }
    }

    /**
     * 매일 오후 5시 (Asia/Seoul) 실행
     * 아직 완료하지 않은 Task 리마인더 전송
     */
    @Scheduled(cron = "0 0 17 * * *", zone = "Asia/Seoul")
    public void sendIncompleteTaskReminder() {
        log.info("TaskSeriesScheduler: Starting incomplete task reminder");
        try {
            LocalDate today = LocalDate.now(clock);
            List<String> incompleteTitles = taskService.getTasksByDate(DEFAULT_USER_ID, today)
                    .stream()
                    .filter(task -> task.getStatus() != TaskStatus.COMPLETED)
                    .map(TaskResponse::getTitle)
                    .toList();

            slackService.sendIncompleteTaskReminder(today, incompleteTitles);
            log.info("TaskSeriesScheduler: Incomplete task reminder sent");

        } catch (Exception e) {
            log.error("TaskSeriesScheduler: Error sending incomplete task reminder", e);
        }
    }
}
