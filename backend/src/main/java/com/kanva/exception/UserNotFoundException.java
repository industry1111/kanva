package com.kanva.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long userId) {
        super("사용자를 찾을 수 없습니다. ID: " + userId);
    }

    public UserNotFoundException(String email) {
        super("사용자를 찾을 수 없습니다. Email: " + email);
    }
}
