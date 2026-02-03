package com.kanva.dto.notification;

import com.kanva.domain.slack.SlackConnection;
import com.kanva.domain.user.User;
import lombok.Builder;
import lombok.Getter;

/**
 * Slack 알림 발송 대상자 정보
 */
@Getter
@Builder
public class SlackTarget {

    private final Long userId;
    private final User user;
    private final String slackUserId;
    private final String teamId;
    private final String botToken;

    public static SlackTarget from(SlackConnection connection) {
        return SlackTarget.builder()
                .userId(connection.getUser().getId())
                .user(connection.getUser())
                .slackUserId(connection.getSlackUserId())
                .teamId(connection.getTeamId())
                .botToken(connection.getBotToken())
                .build();
    }

    public boolean isValid() {
        return slackUserId != null && !slackUserId.isEmpty()
                && botToken != null && !botToken.isEmpty();
    }
}
