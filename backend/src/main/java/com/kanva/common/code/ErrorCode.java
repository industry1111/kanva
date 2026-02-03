package com.kanva.common.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 400 Bad Request
    INVALID_INPUT(400, "입력값이 올바르지 않습니다."),
    MISSING_PARAMETER(400, "필수 파라미터가 누락되었습니다."),
    INVALID_PARAMETER_TYPE(400, "파라미터 형식이 올바르지 않습니다."),
    INVALID_REQUEST_BODY(400, "요청 본문을 읽을 수 없습니다."),
    TASK_STATUS_CHANGE_NOT_ALLOWED(400, "미래 날짜의 Task는 상태를 변경할 수 없습니다."),

    // 401 Unauthorized
    UNAUTHORIZED(401, "인증이 필요합니다."),
    INVALID_CREDENTIALS(401, "이메일 또는 비밀번호가 일치하지 않습니다."),
    OAUTH_FAILED(401, "OAuth 인증에 실패했습니다."),

    // 404 Not Found
    USER_NOT_FOUND(404, "사용자를 찾을 수 없습니다."),
    DAILY_NOTE_NOT_FOUND(404, "노트를 찾을 수 없습니다."),
    TASK_NOT_FOUND(404, "할 일을 찾을 수 없습니다."),

    // 409 Conflict
    DUPLICATE_EMAIL(409, "이미 사용 중인 이메일입니다."),

    // 500 Internal Server Error
    INTERNAL_ERROR(500, "서버 내부 오류가 발생했습니다.");

    private final int status;
    private final String message;
}
