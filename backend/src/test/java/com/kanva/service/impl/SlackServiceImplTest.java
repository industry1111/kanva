package com.kanva.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * SlackServiceImpl 실제 발송 테스트
 * 실제 Slack API를 호출하여 DM을 전송한다.
 *
 * 환경변수 설정 필요:
 * - SLACK_BOT_TOKEN: Slack Bot Token (xoxb-...)
 * - SLACK_USER_ID: Slack User ID (U...)
 */
@EnabledIfEnvironmentVariable(named = "SLACK_BOT_TOKEN", matches = ".+")
class SlackServiceImplTest {

    private SlackServiceImpl slackService;

    @BeforeEach
    void setUp() throws Exception {
        String botToken = System.getenv("SLACK_BOT_TOKEN");
        String userId = System.getenv("SLACK_USER_ID");

        slackService = new SlackServiceImpl();
        setField(slackService, "botToken", botToken);
        setField(slackService, "defaultUserId", userId);
        slackService.init();
    }

    @Test
    @DisplayName("실제 DM 전송 테스트")
    void sendDirectMessage() {
        slackService.sendDirectMessage(System.getenv("SLACK_USER_ID"), "테스트 메시지입니다.");
    }

    @Test
    @DisplayName("오늘의 할 일 알림 전송 테스트")
    void sendDailyTaskNotification() {
        List<String> tasks = List.of(
                "프로젝트 설계 문서 작성",
                "코드 리뷰",
                "팀 미팅 참석"
        );

        slackService.sendDailyTaskNotification(LocalDate.now(), tasks);
    }

    @Test
    @DisplayName("Task 없을 때 알림 전송 테스트")
    void sendDailyTaskNotification_empty() {
        slackService.sendDailyTaskNotification(LocalDate.now(), Collections.emptyList());
    }

    @Test
    @DisplayName("10개 초과 Task 알림 전송 테스트")
    void sendDailyTaskNotification_overflow() {
        List<String> tasks = new ArrayList<>();
        for (int i = 1; i <= 13; i++) {
            tasks.add("Task " + i + " - 작업 내용");
        }

        slackService.sendDailyTaskNotification(LocalDate.now(), tasks);
    }

    @Test
    @DisplayName("마감 임박 알림 전송 테스트")
    void sendDueSoonNotification() {
        List<String> tasks = List.of(
                "API 문서 업데이트 (오늘 마감)",
                "버그 수정 PR 머지 (내일 마감)"
        );

        slackService.sendDueSoonNotification(tasks);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
