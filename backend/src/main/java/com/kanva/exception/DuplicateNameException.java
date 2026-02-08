package com.kanva.exception;

public class DuplicateNameException extends RuntimeException {

    public DuplicateNameException(String name) {
        super("이미 사용 중인 닉네임입니다: " + name);
    }
}
