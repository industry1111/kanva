package com.kanva.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kanva.common.code.ErrorCode;
import com.kanva.common.code.SuccessCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final int code;
    private final String message;
    private final List<FieldError> errors;

    // 성공 응답 - 데이터 포함
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, SuccessCode.SELECT_SUCCESS.getStatus(), SuccessCode.SELECT_SUCCESS.getMessage(), null);
    }

    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(true, data, SuccessCode.INSERT_SUCCESS.getStatus(), SuccessCode.INSERT_SUCCESS.getMessage(), null);
    }

    public static <T> ApiResponse<T> of(T data, SuccessCode successCode) {
        return new ApiResponse<>(true, data, successCode.getStatus(), successCode.getMessage(), null);
    }

    // 실패 응답 - ErrorCode 사용
    public static <T> ApiResponse<T> fail(ErrorCode errorCode) {
        return new ApiResponse<>(false, null, errorCode.getStatus(), errorCode.getMessage(), null);
    }

    public static <T> ApiResponse<T> fail(ErrorCode errorCode, String message) {
        return new ApiResponse<>(false, null, errorCode.getStatus(), message, null);
    }

    public static <T> ApiResponse<T> fail(ErrorCode errorCode, List<FieldError> errors) {
        return new ApiResponse<>(false, null, errorCode.getStatus(), errorCode.getMessage(), errors);
    }

    public static <T> ApiResponse<T> fail(int status, String message) {
        return new ApiResponse<>(false, null, status, message, null);
    }

    public static <T> ApiResponse<T> fail(int status, String message, List<FieldError> errors) {
        return new ApiResponse<>(false, null, status, message, errors);
    }

    @Getter
    @AllArgsConstructor
    public static class FieldError {
        private final String field;
        private final String message;
    }
}
