package com.kanva.service.notification;

import com.kanva.dto.notification.SlackSendResult;
import com.kanva.dto.notification.SlackTarget;
import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.methods.response.conversations.ConversationsOpenResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Slack DM 발송 서비스
 * - 워크스페이스별 botToken으로 MethodsClient 생성
 * - 재시도 로직 포함
 */
@Service
@Slf4j
public class SlackDmSenderService {

    private static final int MAX_RETRY_COUNT = 1;
    private static final long RETRY_DELAY_MS = 700; // 500~1000ms 사이

    // 재시도하지 않을 에러 코드 (인증/권한 관련)
    private static final Set<String> NON_RETRYABLE_ERRORS = Set.of(
            "invalid_auth",
            "account_inactive",
            "token_revoked",
            "token_expired",
            "no_permission",
            "missing_scope",
            "not_authed",
            "user_not_found",
            "channel_not_found"
    );

    private final Slack slack = Slack.getInstance();

    // teamId → (slackUserId → dmChannelId) 캐시
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, String>> dmChannelCache
            = new ConcurrentHashMap<>();

    /**
     * 재시도 포함 메시지 발송
     */
    public SlackSendResult sendWithRetry(SlackTarget target, String message) {
        int attempt = 0;
        String lastError = null;

        while (attempt <= MAX_RETRY_COUNT) {
            try {
                SendResult result = sendMessage(target, message);
                if (result.success) {
                    log.info("Slack DM sent successfully to user {} (attempt {})",
                            target.getSlackUserId(), attempt + 1);
                    return SlackSendResult.success();
                }

                lastError = result.errorCode;
                log.warn("Slack API error for user {}: {} (attempt {})",
                        target.getSlackUserId(), lastError, attempt + 1);

                if (!isRetryable(lastError)) {
                    log.error("Non-retryable Slack error: {}", lastError);
                    return SlackSendResult.fail(lastError, attempt, false);
                }

                attempt++;
                if (attempt <= MAX_RETRY_COUNT) {
                    Thread.sleep(RETRY_DELAY_MS);
                }

            } catch (SlackApiException e) {
                lastError = e.getMessage();
                log.warn("Slack API exception for user {}: {} (attempt {})",
                        target.getSlackUserId(), lastError, attempt + 1);

                attempt++;
                if (attempt <= MAX_RETRY_COUNT) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return SlackSendResult.fail("Interrupted during retry", attempt, false);
                    }
                }

            } catch (IOException e) {
                lastError = "IO error: " + e.getMessage();
                log.warn("IO error sending Slack DM to user {}: {} (attempt {})",
                        target.getSlackUserId(), e.getMessage(), attempt + 1);

                attempt++;
                if (attempt <= MAX_RETRY_COUNT) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return SlackSendResult.fail("Interrupted during retry", attempt, false);
                    }
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return SlackSendResult.fail("Interrupted", attempt, false);
            }
        }

        log.error("Failed to send Slack DM to user {} after {} attempts: {}",
                target.getSlackUserId(), attempt, lastError);
        return SlackSendResult.fail(lastError, attempt, true);
    }

    /**
     * 메시지 발송 (단일 시도)
     */
    private SendResult sendMessage(SlackTarget target, String message)
            throws SlackApiException, IOException {

        MethodsClient client = slack.methods(target.getBotToken());

        // DM 채널 열기 또는 캐시에서 조회
        ChannelResult channelResult = getOrOpenDmChannel(client, target);
        if (!channelResult.success) {
            return SendResult.fail(channelResult.errorCode);
        }

        // 메시지 발송
        ChatPostMessageResponse response = client.chatPostMessage(req -> req
                .channel(channelResult.channelId)
                .text(message)
        );

        if (!response.isOk()) {
            return SendResult.fail(response.getError());
        }

        return SendResult.success();
    }

    /**
     * DM 채널 ID 조회 또는 생성
     */
    private ChannelResult getOrOpenDmChannel(MethodsClient client, SlackTarget target)
            throws SlackApiException, IOException {

        // 캐시 확인
        ConcurrentHashMap<String, String> teamCache = dmChannelCache
                .computeIfAbsent(target.getTeamId(), k -> new ConcurrentHashMap<>());

        String cached = teamCache.get(target.getSlackUserId());
        if (cached != null) {
            return ChannelResult.success(cached);
        }

        // DM 채널 열기
        ConversationsOpenResponse response = client.conversationsOpen(req -> req
                .users(List.of(target.getSlackUserId()))
        );

        if (!response.isOk()) {
            log.error("Failed to open DM channel for user {}: {}",
                    target.getSlackUserId(), response.getError());
            return ChannelResult.fail(response.getError());
        }

        String channelId = response.getChannel().getId();
        teamCache.put(target.getSlackUserId(), channelId);

        log.debug("DM channel opened: teamId={}, userId={}, channelId={}",
                target.getTeamId(), target.getSlackUserId(), channelId);

        return ChannelResult.success(channelId);
    }

    /**
     * 재시도 가능한 에러인지 확인
     */
    private boolean isRetryable(String errorCode) {
        return errorCode == null || !NON_RETRYABLE_ERRORS.contains(errorCode);
    }

    // 내부 결과 클래스
    private static class SendResult {
        final boolean success;
        final String errorCode;

        private SendResult(boolean success, String errorCode) {
            this.success = success;
            this.errorCode = errorCode;
        }

        static SendResult success() {
            return new SendResult(true, null);
        }

        static SendResult fail(String errorCode) {
            return new SendResult(false, errorCode);
        }
    }

    private static class ChannelResult {
        final boolean success;
        final String channelId;
        final String errorCode;

        private ChannelResult(boolean success, String channelId, String errorCode) {
            this.success = success;
            this.channelId = channelId;
            this.errorCode = errorCode;
        }

        static ChannelResult success(String channelId) {
            return new ChannelResult(true, channelId, null);
        }

        static ChannelResult fail(String errorCode) {
            return new ChannelResult(false, null, errorCode);
        }
    }
}
