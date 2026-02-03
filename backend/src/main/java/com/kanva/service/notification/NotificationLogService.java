package com.kanva.service.notification;

import com.kanva.domain.notification.NotificationLog;
import com.kanva.domain.notification.NotificationLogRepository;
import com.kanva.domain.notification.NotificationSlot;
import com.kanva.dto.notification.SlackSendResult;
import com.kanva.dto.notification.SlackTarget;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * 알림 발송 이력 기록 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationLogService {

    private final NotificationLogRepository notificationLogRepository;

    /**
     * 발송 결과 기록
     * - 별도 트랜잭션으로 실행하여 발송 실패와 무관하게 로그 저장
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logResult(SlackTarget target, NotificationSlot slot, LocalDate date,
                          SlackSendResult result) {
        try {
            NotificationLog logEntry;

            if (result.isSuccess()) {
                logEntry = NotificationLog.success(
                        target.getUser(),
                        slot,
                        date,
                        target.getSlackUserId(),
                        target.getTeamId()
                );
            } else {
                logEntry = NotificationLog.fail(
                        target.getUser(),
                        slot,
                        date,
                        target.getSlackUserId(),
                        target.getTeamId(),
                        truncateErrorMessage(result.getErrorMessage()),
                        result.getRetryCount()
                );
            }

            notificationLogRepository.save(logEntry);

            log.debug("Notification log saved: userId={}, slot={}, date={}, result={}",
                    target.getUserId(), slot, date, result.isSuccess() ? "SUCCESS" : "FAIL");

        } catch (Exception e) {
            // 로그 저장 실패해도 전체 프로세스에 영향 주지 않음
            log.error("Failed to save notification log for user {}: {}",
                    target.getUserId(), e.getMessage());
        }
    }

    private String truncateErrorMessage(String message) {
        if (message == null) {
            return null;
        }
        return message.length() > 500 ? message.substring(0, 500) : message;
    }
}
