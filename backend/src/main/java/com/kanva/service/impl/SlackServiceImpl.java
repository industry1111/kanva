package com.kanva.service.impl;

import com.kanva.service.SlackService;
import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.methods.response.conversations.ConversationsOpenResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@Slf4j
public class SlackServiceImpl implements SlackService {

    @Value("${slack.bot.token:}")
    private String botToken;

    @Value("${slack.user.id:}")
    private String defaultUserId;

    private final Slack slack = Slack.getInstance();

    @Override
    public void sendDirectMessage(String slackUserId, String message) {
        if (botToken.isEmpty()) {
            log.warn("Slack bot token not configured, skipping notification");
            return;
        }

        String targetUserId = (slackUserId == null || slackUserId.isEmpty()) ? defaultUserId : slackUserId;
        if (targetUserId.isEmpty()) {
            log.warn("Slack user ID not specified, skipping notification");
            return;
        }

        try {
            MethodsClient client = slack.methods(botToken);

            // 1. DM 채널 열기
            ConversationsOpenResponse openResponse = client.conversationsOpen(req -> req
                    .users(List.of(targetUserId))
            );

            if (!openResponse.isOk()) {
                log.error("Failed to open DM channel: {}", openResponse.getError());
                return;
            }

            String dmChannelId = openResponse.getChannel().getId();

            // 2. 메시지 전송
            ChatPostMessageResponse response = client.chatPostMessage(req -> req
                    .channel(dmChannelId)
                    .text(message)
            );

            if (response.isOk()) {
                log.info("Slack DM sent successfully to user: {}", targetUserId);
            } else {
                log.error("Failed to send Slack DM: {}", response.getError());
            }

        } catch (Exception e) {
            log.error("Error sending Slack DM", e);
        }
    }

    @Override
    public void sendDailyTaskNotification(LocalDate date, List<String> taskTitles) {
        if (defaultUserId.isEmpty()) {
            log.warn("Slack user ID not configured, skipping notification");
            return;
        }

        String dateStr = date.format(
                DateTimeFormatter.ofPattern("yyyy년 M월 d일 (E)", Locale.KOREAN)
        );

        StringBuilder message = new StringBuilder();
        message.append("📅 ").append(dateStr).append(" 오늘의 할 일\n\n");

        if (taskTitles.isEmpty()) {
            message.append("등록된 Task가 없습니다. 오늘 하루도 화이팅! 🎉");
        } else {
            for (String title : taskTitles) {
                message.append("☐ ").append(title).append("\n");
            }
            message.append("\n총 ").append(taskTitles.size()).append("개의 Task가 있습니다.");
        }

        sendDirectMessage(defaultUserId, message.toString());
    }

    @Override
    public void sendDueSoonNotification(List<String> taskTitles) {
        if (defaultUserId.isEmpty() || taskTitles.isEmpty()) {
            return;
        }

        StringBuilder message = new StringBuilder();
        message.append("⚠️ 마감 임박 Task\n\n");

        for (String title : taskTitles) {
            message.append("• ").append(title).append("\n");
        }

        message.append("\n서두르세요! 💪");

        sendDirectMessage(defaultUserId, message.toString());
    }
}
