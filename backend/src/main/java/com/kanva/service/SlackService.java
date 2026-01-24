package com.kanva.service;

import java.time.LocalDate;
import java.util.List;

public interface SlackService {

    /**
     * Slack DM으로 메시지 전송
     */
    void sendDirectMessage(String slackUserId, String message);

    /**
     * 오늘의 Task 알림 전송
     */
    void sendDailyTaskNotification(LocalDate date, List<String> taskTitles);

    /**
     * 미완료 Task 리마인더 전송
     */
    void sendIncompleteTaskReminder(LocalDate date, List<String> taskTitles);

    /**
     * 마감 임박 Task 알림 전송
     */
    void sendDueSoonNotification(List<String> taskTitles);
}
