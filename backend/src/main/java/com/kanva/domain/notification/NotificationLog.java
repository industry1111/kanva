package com.kanva.domain.notification;

import com.kanva.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Slack 알림 발송 이력
 */
@Entity
@Table(
    name = "notification_logs",
    indexes = {
        @Index(name = "idx_notification_log_user_date_slot", columnList = "user_id, notification_date, slot"),
        @Index(name = "idx_notification_log_sent_at", columnList = "sent_at")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationSlot slot;

    @Column(name = "notification_date", nullable = false)
    private LocalDate notificationDate;

    @Column(nullable = false, length = 50)
    private String slackUserId;

    @Column(nullable = false, length = 50)
    private String teamId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationResult result;

    @Column(length = 500)
    private String errorMessage;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @Column(name = "retry_count")
    @Builder.Default
    private int retryCount = 0;

    public static NotificationLog success(User user, NotificationSlot slot, LocalDate date,
                                          String slackUserId, String teamId) {
        return NotificationLog.builder()
                .user(user)
                .slot(slot)
                .notificationDate(date)
                .slackUserId(slackUserId)
                .teamId(teamId)
                .result(NotificationResult.SUCCESS)
                .sentAt(LocalDateTime.now())
                .build();
    }

    public static NotificationLog fail(User user, NotificationSlot slot, LocalDate date,
                                       String slackUserId, String teamId,
                                       String errorMessage, int retryCount) {
        return NotificationLog.builder()
                .user(user)
                .slot(slot)
                .notificationDate(date)
                .slackUserId(slackUserId)
                .teamId(teamId)
                .result(NotificationResult.FAIL)
                .errorMessage(errorMessage)
                .sentAt(LocalDateTime.now())
                .retryCount(retryCount)
                .build();
    }
}
