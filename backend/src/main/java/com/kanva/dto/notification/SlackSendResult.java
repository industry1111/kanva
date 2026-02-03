package com.kanva.dto.notification;

import lombok.Builder;
import lombok.Getter;

/**
 * Slack 메시지 발송 결과
 */
@Getter
@Builder
public class SlackSendResult {

    private final boolean success;
    private final String errorMessage;
    private final int retryCount;
    private final boolean retryable;

    public static SlackSendResult success() {
        return SlackSendResult.builder()
                .success(true)
                .retryCount(0)
                .retryable(false)
                .build();
    }

    public static SlackSendResult fail(String errorMessage, int retryCount, boolean retryable) {
        return SlackSendResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .retryCount(retryCount)
                .retryable(retryable)
                .build();
    }
}
