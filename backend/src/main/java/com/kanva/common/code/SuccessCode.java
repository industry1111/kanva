package com.kanva.common.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SuccessCode {

    SELECT_SUCCESS(200, "조회 성공"),
    INSERT_SUCCESS(201, "생성 성공"),
    UPDATE_SUCCESS(200, "수정 성공"),
    DELETE_SUCCESS(200, "삭제 성공");

    private final int status;
    private final String message;
}
