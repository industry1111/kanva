package com.kanva.exception;

public class DailyNoteNotFoundException extends RuntimeException {

    public DailyNoteNotFoundException(Long dailyNoteId) {
        super("DailyNote를 찾을 수 없습니다. ID: " + dailyNoteId);
    }
}
