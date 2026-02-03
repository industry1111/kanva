-- Notification Log 테이블
-- Slack 알림 발송 이력 저장

CREATE TABLE notification_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    slot VARCHAR(20) NOT NULL,  -- MORNING, EVENING
    notification_date DATE NOT NULL,
    slack_user_id VARCHAR(50) NOT NULL,
    team_id VARCHAR(50) NOT NULL,
    result VARCHAR(20) NOT NULL,  -- SUCCESS, FAIL
    error_message VARCHAR(500),
    retry_count INTEGER DEFAULT 0,
    sent_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 중복 발송 방지 및 조회 최적화 인덱스
CREATE INDEX idx_notification_log_user_date_slot
    ON notification_logs(user_id, notification_date, slot);

-- 발송 시간 기준 조회 인덱스
CREATE INDEX idx_notification_log_sent_at
    ON notification_logs(sent_at);

-- 결과별 조회 인덱스 (실패 분석용)
CREATE INDEX idx_notification_log_result
    ON notification_logs(result);

COMMENT ON TABLE notification_logs IS 'Slack 알림 발송 이력';
COMMENT ON COLUMN notification_logs.slot IS '발송 시간대 (MORNING: 08:00, EVENING: 17:00)';
COMMENT ON COLUMN notification_logs.result IS '발송 결과 (SUCCESS, FAIL)';
COMMENT ON COLUMN notification_logs.retry_count IS '재시도 횟수';
