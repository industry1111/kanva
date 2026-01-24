package com.kanva.service.impl;

import com.kanva.service.SlackService;
import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.methods.response.conversations.ConversationsOpenResponse;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Slack DM 전송 서비스
 *
 * 흐름: conversations.open → chat.postMessage
 * - conversations.open: 봇과 사용자 간 1:1 DM 채널을 열거나 기존 채널 ID를 반환
 * - chat.postMessage: 해당 DM 채널에 메시지 전송
 *
 * Incoming Webhook이 아닌 DM 방식을 사용하는 이유:
 * - 개인 생산성 도구이므로 사용자에게 직접 알림 전달이 핵심
 * - 채널이 아닌 개인 DM으로 전송해야 다른 사용자에게 노출되지 않음
 * - 향후 다중 사용자 확장 시 userId별로 개별 DM 전송 가능
 */
@Service
@Slf4j
public class SlackServiceImpl implements SlackService {

    private static final int MAX_DISPLAY_TASKS = 10;

    @Value("${slack.bot.token:}")
    private String botToken;

    @Value("${slack.user.id:}")
    private String defaultUserId;

    /** MethodsClient 재사용 (정책 2: 매 호출마다 생성하지 않음) */
    private MethodsClient client;

    /** DM 채널 ID 캐시 - userId → dmChannelId (정책 1: 매번 열지 않음) */
    private final ConcurrentHashMap<String, String> dmChannelCache = new ConcurrentHashMap<>();

    @PostConstruct
    void init() {
        if (!botToken.isEmpty()) {
            this.client = Slack.getInstance().methods(botToken);
            log.info("Slack client initialized");
        } else {
            log.debug("Slack bot token not configured, Slack notifications disabled");
        }
    }

    @Override
    public void sendDirectMessage(String slackUserId, String message) {
        // 정책 3: 설정 없으면 조용히 skip
        if (client == null) {
            return;
        }

        String targetUserId = (slackUserId != null && !slackUserId.isEmpty()) ? slackUserId : defaultUserId;
        if (targetUserId.isEmpty()) {
            return;
        }

        try {
            String dmChannelId = getOrOpenDmChannel(targetUserId);
            if (dmChannelId == null) {
                return;
            }

            ChatPostMessageResponse response = client.chatPostMessage(req -> req
                    .channel(dmChannelId)
                    .text(message)
            );

            if (!response.isOk()) {
                // 정책 4: 실패 시 로그만
                log.error("Slack chat.postMessage failed: {}", response.getError());
            }

        } catch (Exception e) {
            // 정책 4, 6: 예외는 로그만 남기고 비즈니스 로직에 전파하지 않음
            log.error("Slack DM send error for user {}: {}", targetUserId, e.getMessage());
        }
    }

    @Override
    public void sendDailyTaskNotification(LocalDate date, List<String> taskTitles) {
        if (client == null || defaultUserId.isEmpty()) {
            return;
        }

        String dateStr = date.format(
                DateTimeFormatter.ofPattern("yyyy년 M월 d일 (E)", Locale.KOREAN)
        );

        StringBuilder msg = new StringBuilder();
        msg.append("📅 ").append(dateStr).append(" 오늘의 할 일\n\n");

        if (taskTitles.isEmpty()) {
            msg.append("등록된 Task가 없습니다.");
        } else {
            // 정책 5: 상위 N개만 표시, 나머지 요약
            int displayCount = Math.min(taskTitles.size(), MAX_DISPLAY_TASKS);
            for (int i = 0; i < displayCount; i++) {
                msg.append("• ").append(taskTitles.get(i)).append("\n");
            }
            int remaining = taskTitles.size() - displayCount;
            if (remaining > 0) {
                msg.append("  ...외 ").append(remaining).append("개\n");
            }
            msg.append("\n총 ").append(taskTitles.size()).append("개");
        }

        sendDirectMessage(defaultUserId, msg.toString());
    }

    @Override
    public void sendIncompleteTaskReminder(LocalDate date, List<String> taskTitles) {
        if (defaultUserId.isEmpty()) {
            return;
        }

        String dateStr = date.format(
                DateTimeFormatter.ofPattern("yyyy년 M월 d일 (E)", Locale.KOREAN)
        );

        StringBuilder message = new StringBuilder();
        message.append("⏰ ").append(dateStr).append(" 미완료 Task 리마인더\n\n");

        if (taskTitles.isEmpty()) {
            message.append("오늘 할 일을 모두 완료했습니다! 🎉");
        } else {
            for (String title : taskTitles) {
                message.append("☐ ").append(title).append("\n");
            }
            message.append("\n아직 ").append(taskTitles.size()).append("개의 Task가 남아있습니다.");
        }

        sendDirectMessage(defaultUserId, message.toString());
    }

    @Override
    public void sendDueSoonNotification(List<String> taskTitles) {
        if (client == null || defaultUserId.isEmpty() || taskTitles.isEmpty()) {
            return;
        }

        StringBuilder msg = new StringBuilder();
        msg.append("⚠️ 마감 임박 Task\n\n");

        int displayCount = Math.min(taskTitles.size(), MAX_DISPLAY_TASKS);
        for (int i = 0; i < displayCount; i++) {
            msg.append("• ").append(taskTitles.get(i)).append("\n");
        }
        int remaining = taskTitles.size() - displayCount;
        if (remaining > 0) {
            msg.append("  ...외 ").append(remaining).append("개");
        }

        sendDirectMessage(defaultUserId, msg.toString());
    }

    /**
     * DM 채널 ID를 캐시에서 조회하거나, 없으면 conversations.open으로 열어서 캐시
     *
     * conversations.open은 이미 열린 채널이 있으면 기존 채널을 반환하므로 멱등하지만,
     * 불필요한 API 호출을 줄이기 위해 로컬 캐시를 사용한다.
     */
    private String getOrOpenDmChannel(String userId) {
        String cached = dmChannelCache.get(userId);
        if (cached != null) {
            return cached;
        }

        try {
            ConversationsOpenResponse response = client.conversationsOpen(req -> req
                    .users(List.of(userId))
            );

            if (!response.isOk()) {
                log.error("Slack conversations.open failed for user {}: {}", userId, response.getError());
                return null;
            }

            String channelId = response.getChannel().getId();
            dmChannelCache.put(userId, channelId);
            log.debug("DM channel opened and cached: userId={}, channelId={}", userId, channelId);
            return channelId;

        } catch (Exception e) {
            log.error("Slack conversations.open error for user {}: {}", userId, e.getMessage());
            return null;
        }
    }
}
