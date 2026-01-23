package com.kanva.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * SlackServiceImpl 실제 발송 테스트
 * application-local.properties에서 토큰을 읽어 실제 Slack DM을 전송한다.
 * 설정이 없으면 테스트를 skip한다.
 */
class SlackServiceImplTest {

    private SlackServiceImpl slackService;
    private String userId;

    @BeforeEach
    void setUp() throws Exception {
        Properties props = new Properties();
        InputStream is = getClass().getClassLoader().getResourceAsStream("application-local.properties");
        assumeTrue(is != null, "application-local.properties not found");
        props.load(is);

        String botToken = props.getProperty("slack.bot.token", "");
        userId = props.getProperty("slack.user.id", "");
        assumeTrue(!botToken.isEmpty(), "slack.bot.token not configured");

        slackService = new SlackServiceImpl();
        setField(slackService, "botToken", botToken);
        setField(slackService, "defaultUserId", userId);
        slackService.init();
    }

    @Test
    @DisplayName("실제 DM 전송 테스트")
    void sendDirectMessage() {
        slackService.sendDirectMessage(userId, "테스트 메시지입니다.");
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
