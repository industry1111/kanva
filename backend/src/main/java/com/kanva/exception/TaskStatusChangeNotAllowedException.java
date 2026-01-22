package com.kanva.exception;

public class TaskStatusChangeNotAllowedException extends RuntimeException {

    public TaskStatusChangeNotAllowedException() {
        super("미래 날짜의 Task는 상태를 변경할 수 없습니다.");
    }
}
