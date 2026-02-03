package com.kanva.scheduler;

import com.kanva.service.TaskSeriesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Task Series 스케줄러
 * - 반복 Task 인스턴스 자동 생성
 *
 * 알림 발송은 NotificationScheduler에서 처리
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TaskSeriesScheduler {

    private final TaskSeriesService taskSeriesService;

    /**
     * 매일 오전 7시 55분 (Asia/Seoul) 실행
     * - 오늘 날짜에 대해 ACTIVE 시리즈의 Task 인스턴스 생성
     * - 알림 발송(08:00) 전에 Task 생성이 완료되도록 5분 전 실행
     */
    @Scheduled(cron = "0 55 7 * * *", zone = "Asia/Seoul")
    public void generateDailyTasks() {
        log.info("TaskSeriesScheduler: Starting daily task generation");
        try {
            taskSeriesService.generateTodayTasks();
            log.info("TaskSeriesScheduler: Daily task generation completed");
        } catch (Exception e) {
            log.error("TaskSeriesScheduler: Error during daily task generation", e);
        }
    }
}
