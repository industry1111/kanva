package com.kanva.domain.notification;

/**
 * 알림 발송 시간대
 */
public enum NotificationSlot {
    /** 오전 08:00 - 오늘 할 일 알림 */
    MORNING,
    /** 오후 17:00 - 미완료 Task 리마인더 */
    EVENING
}
