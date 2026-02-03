package com.kanva.domain.slack;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SlackConnectionRepository extends JpaRepository<SlackConnection, Long> {

    Optional<SlackConnection> findByUserId(Long userId);

    Optional<SlackConnection> findBySlackUserId(String slackUserId);

    List<SlackConnection> findAllByNotificationsEnabledTrue();

    boolean existsByUserId(Long userId);

    /**
     * 알림 발송 대상자 조회
     * - 알림 활성화된 사용자
     * - botToken과 slackUserId가 모두 존재
     * - 제외할 사용자 ID 목록에 없는 사용자
     */
    @Query("""
        SELECT sc FROM SlackConnection sc
        JOIN FETCH sc.user
        WHERE sc.notificationsEnabled = true
          AND sc.botToken IS NOT NULL
          AND sc.slackUserId IS NOT NULL
          AND sc.user.id NOT IN :excludeUserIds
        """)
    List<SlackConnection> findNotificationTargets(@Param("excludeUserIds") Set<Long> excludeUserIds);

    /**
     * 알림 발송 대상자 조회 (제외 목록 없음)
     */
    @Query("""
        SELECT sc FROM SlackConnection sc
        JOIN FETCH sc.user
        WHERE sc.notificationsEnabled = true
          AND sc.botToken IS NOT NULL
          AND sc.slackUserId IS NOT NULL
        """)
    List<SlackConnection> findAllNotificationTargets();
}
