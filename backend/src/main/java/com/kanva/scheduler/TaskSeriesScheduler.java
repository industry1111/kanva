package com.kanva.scheduler;

import com.kanva.service.TaskSeriesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TaskSeriesScheduler {

    private final TaskSeriesService taskSeriesService;

    /**
     * 매일 오전 8시 (Asia/Seoul) 실행
     * 오늘 날짜에 대해 ACTIVE 시리즈의 Task 인스턴스 생성
     */
    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Seoul")
    public void generateDailyTasks() {
        log.info("TaskSeriesScheduler: Starting daily task generation");
        try {
            taskSeriesService.generateTodayTasks();
            log.info("TaskSeriesScheduler: Daily task generation completed successfully");
        } catch (Exception e) {
            log.error("TaskSeriesScheduler: Error during daily task generation", e);
        }
    }
}
