package com.kanva.scheduler;

import com.kanva.domain.slack.SlackConnection;
import com.kanva.domain.slack.SlackConnectionRepository;
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
    private final SlackConnectionRepository slackConnectionRepository;
    private final Clock clock;

    /**
     * 매일 오전 8시 (Asia/Seoul) 실행
     * 1. 오늘 날짜에 대해 ACTIVE 시리즈의 Task 인스턴스 생성
     * 2. Slack DM으로 오늘의 할 일 알림 전송 (알림 활성화된 사용자 대상)
     */
    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Seoul")
    public void generateDailyTasks() {
        log.info("TaskSeriesScheduler: Starting daily task generation");
        try {
            taskSeriesService.generateTodayTasks();
            log.info("TaskSeriesScheduler: Daily task generation completed");

            LocalDate today = LocalDate.now(clock);
            sendMorningNotifications(today);

        } catch (Exception e) {
            log.error("TaskSeriesScheduler: Error during daily task generation", e);
        }
    }

    /**
     * 매일 오후 5시 (Asia/Seoul) 실행
     * 아직 완료하지 않은 Task 리마인더 전송 (알림 활성화된 사용자 대상)
     */
    @Scheduled(cron = "0 0 17 * * *", zone = "Asia/Seoul")
    public void sendIncompleteTaskReminder() {
        log.info("TaskSeriesScheduler: Starting incomplete task reminder");
        try {
            LocalDate today = LocalDate.now(clock);
            sendEveningReminders(today);

        } catch (Exception e) {
            log.error("TaskSeriesScheduler: Error sending incomplete task reminder", e);
        }
    }

    private void sendMorningNotifications(LocalDate today) {
        List<SlackConnection> connections = slackConnectionRepository.findAllByNotificationsEnabledTrue();

        for (SlackConnection connection : connections) {
            try {
                List<String> taskTitles = taskService.getTasksByDate(connection.getUser().getId(), today)
                        .stream()
                        .map(TaskResponse::getTitle)
                        .toList();

                slackService.sendDailyTaskNotification(today, taskTitles);
                log.info("TaskSeriesScheduler: Morning notification sent to user {}", connection.getUser().getId());

            } catch (Exception e) {
                log.error("TaskSeriesScheduler: Failed to send morning notification to user {}",
                        connection.getUser().getId(), e);
            }
        }
    }

    private void sendEveningReminders(LocalDate today) {
        List<SlackConnection> connections = slackConnectionRepository.findAllByNotificationsEnabledTrue();

        for (SlackConnection connection : connections) {
            try {
                List<String> incompleteTitles = taskService.getTasksByDate(connection.getUser().getId(), today)
                        .stream()
                        .filter(task -> task.getStatus() != TaskStatus.COMPLETED)
                        .map(TaskResponse::getTitle)
                        .toList();

                slackService.sendIncompleteTaskReminder(today, incompleteTitles);
                log.info("TaskSeriesScheduler: Incomplete task reminder sent to user {}", connection.getUser().getId());

            } catch (Exception e) {
                log.error("TaskSeriesScheduler: Failed to send evening reminder to user {}",
                        connection.getUser().getId(), e);
            }
        }
    }
}
