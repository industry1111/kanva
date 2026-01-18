package com.kanva.common.response;

import com.kanva.common.code.SuccessCode;
import lombok.Getter;

@Getter
public class ApiResponse<T> {

    private final T data;
    private final int code;
    private final String message;

    private ApiResponse(T data, SuccessCode successCode) {
        this.data = data;
        this.code = successCode.getStatus();
        this.message = successCode.getMessage();
    }

    public static <T> ApiResponse<T> of(T data, SuccessCode successCode) {
        return new ApiResponse<>(data, successCode);
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(data, SuccessCode.SELECT_SUCCESS);
    }

    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(data, SuccessCode.INSERT_SUCCESS);
    }
}
