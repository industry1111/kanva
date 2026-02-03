package com.kanva.scheduler;

import com.kanva.domain.notification.NotificationSlot;
import com.kanva.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;

/**
 * Slack 알림 스케줄러
 * - 08:00 AM: 오늘 할 일 알림
 * - 05:00 PM: 미완료 Task 리마인더
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final NotificationService notificationService;
    private final Clock clock;

    /**
     * 매일 오전 8시 (KST) - 오늘 할 일 알림
     */
    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Seoul")
    public void sendMorningNotifications() {
        log.info("NotificationScheduler: Starting morning notifications");

        try {
            LocalDate today = LocalDate.now(clock);
            notificationService.sendNotifications(NotificationSlot.MORNING, today);
        } catch (Exception e) {
            // 스케줄러 안정성: 예외가 스케줄러를 중단시키지 않도록 함
            log.error("NotificationScheduler: Morning notification failed", e);
        }

        log.info("NotificationScheduler: Morning notifications completed");
    }

    /**
     * 매일 오후 5시 (KST) - 미완료 Task 리마인더
     */
    @Scheduled(cron = "0 0 17 * * *", zone = "Asia/Seoul")
    public void sendEveningReminders() {
        log.info("NotificationScheduler: Starting evening reminders");

        try {
            LocalDate today = LocalDate.now(clock);
            notificationService.sendNotifications(NotificationSlot.EVENING, today);
        } catch (Exception e) {
            // 스케줄러 안정성: 예외가 스케줄러를 중단시키지 않도록 함
            log.error("NotificationScheduler: Evening reminder failed", e);
        }

        log.info("NotificationScheduler: Evening reminders completed");
    }
}
