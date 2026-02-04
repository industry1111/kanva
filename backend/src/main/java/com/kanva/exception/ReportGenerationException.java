package com.kanva.exception;

public class ReportGenerationException extends RuntimeException {

    public ReportGenerationException(String message) {
        super("리포트 생성에 실패했습니다: " + message);
    }

    public ReportGenerationException(String message, Throwable cause) {
        super("리포트 생성에 실패했습니다: " + message, cause);
    }
}
