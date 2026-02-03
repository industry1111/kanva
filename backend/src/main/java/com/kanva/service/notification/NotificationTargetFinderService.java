package com.kanva.service.notification;

import com.kanva.domain.notification.NotificationLogRepository;
import com.kanva.domain.notification.NotificationSlot;
import com.kanva.domain.slack.SlackConnection;
import com.kanva.domain.slack.SlackConnectionRepository;
import com.kanva.dto.notification.SlackTarget;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 알림 발송 대상자 조회 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class NotificationTargetFinderService {

    private final SlackConnectionRepository slackConnectionRepository;
    private final NotificationLogRepository notificationLogRepository;

    /**
     * 발송 대상자 조회
     *
     * @param slot 발송 시간대 (MORNING/EVENING)
     * @param date 발송 날짜
     * @return 발송 대상자 목록
     */
    public List<SlackTarget> findTargets(NotificationSlot slot, LocalDate date) {
        // 이미 성공적으로 발송된 사용자 ID 조회
        Set<Long> alreadySentUserIds = notificationLogRepository.findSuccessfullySentUserIds(date, slot);

        List<SlackConnection> connections;
        if (alreadySentUserIds.isEmpty()) {
            connections = slackConnectionRepository.findAllNotificationTargets();
        } else {
            connections = slackConnectionRepository.findNotificationTargets(alreadySentUserIds);
        }

        List<SlackTarget> targets = connections.stream()
                .map(SlackTarget::from)
                .filter(SlackTarget::isValid)
                .toList();

        log.info("Found {} notification targets for {} on {}",
                targets.size(), slot, date);

        return targets;
    }
}
