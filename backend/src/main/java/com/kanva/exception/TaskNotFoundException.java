package com.kanva.exception;

public class TaskNotFoundException extends RuntimeException {

    public TaskNotFoundException(Long taskId) {
        super("Task를 찾을 수 없습니다. ID: " + taskId);
    }
}
