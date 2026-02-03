package com.kanva.service.notification;

import com.kanva.domain.notification.NotificationSlot;
import com.kanva.domain.task.TaskStatus;
import com.kanva.dto.notification.SlackSendResult;
import com.kanva.dto.notification.SlackTarget;
import com.kanva.dto.task.TaskResponse;
import com.kanva.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 알림 발송 서비스
 * - 전체 발송 플로우 조율
 * - 사용자별 독립 발송 (한 사용자 실패가 다른 사용자에 영향 없음)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationTargetFinderService targetFinderService;
    private final SlackDmSenderService slackDmSenderService;
    private final NotificationLogService notificationLogService;
    private final NotificationMessageBuilder messageBuilder;
    private final TaskService taskService;

    /**
     * 지정된 슬롯의 알림 발송
     */
    public void sendNotifications(NotificationSlot slot, LocalDate date) {
        log.info("Starting {} notifications for {}", slot, date);

        List<SlackTarget> targets = targetFinderService.findTargets(slot, date);

        if (targets.isEmpty()) {
            log.info("No notification targets found for {} on {}", slot, date);
            return;
        }

        int successCount = 0;
        int failCount = 0;

        for (SlackTarget target : targets) {
            try {
                boolean success = sendToTarget(target, slot, date);
                if (success) {
                    successCount++;
                } else {
                    failCount++;
                }
            } catch (Exception e) {
                // 개별 사용자 발송 실패가 다른 사용자에 영향 주지 않음
                log.error("Unexpected error sending notification to user {}: {}",
                        target.getUserId(), e.getMessage());
                failCount++;
            }
        }

        log.info("Completed {} notifications for {}: {} success, {} fail",
                slot, date, successCount, failCount);
    }

    /**
     * 단일 대상자에게 발송
     */
    private boolean sendToTarget(SlackTarget target, NotificationSlot slot, LocalDate date) {
        log.debug("Sending {} notification to user {}", slot, target.getUserId());

        // Task 목록 조회
        List<String> taskTitles = getTaskTitlesForSlot(target.getUserId(), slot, date);

        // 메시지 생성
        String message = messageBuilder.buildMessage(slot, date, taskTitles);

        // 발송 (재시도 포함)
        SlackSendResult result = slackDmSenderService.sendWithRetry(target, message);

        // 로그 기록
        notificationLogService.logResult(target, slot, date, result);

        return result.isSuccess();
    }

    /**
     * 슬롯에 따른 Task 목록 조회
     */
    private List<String> getTaskTitlesForSlot(Long userId, NotificationSlot slot, LocalDate date) {
        List<TaskResponse> tasks = taskService.getTasksByDate(userId, date);

        return switch (slot) {
            case MORNING -> tasks.stream()
                    .map(TaskResponse::getTitle)
                    .toList();
            case EVENING -> tasks.stream()
                    .filter(task -> task.getStatus() != TaskStatus.COMPLETED)
                    .map(TaskResponse::getTitle)
                    .toList();
        };
    }
}
