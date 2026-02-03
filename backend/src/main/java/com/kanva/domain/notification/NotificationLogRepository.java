package com.kanva.domain.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Set;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

    /**
     * 특정 날짜, 시간대에 성공적으로 발송된 사용자 ID 목록 조회
     */
    @Query("""
        SELECT nl.user.id FROM NotificationLog nl
        WHERE nl.notificationDate = :date
          AND nl.slot = :slot
          AND nl.result = 'SUCCESS'
        """)
    Set<Long> findSuccessfullySentUserIds(
            @Param("date") LocalDate date,
            @Param("slot") NotificationSlot slot
    );

    /**
     * 특정 사용자의 특정 날짜, 시간대 발송 여부 확인
     */
    boolean existsByUserIdAndNotificationDateAndSlotAndResult(
            Long userId,
            LocalDate notificationDate,
            NotificationSlot slot,
            NotificationResult result
    );
}
