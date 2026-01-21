package com.kanva.dto.dailynote;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class DailyNoteRequest {

    @NotNull(message = "날짜를 입력해 주세요.")
    private LocalDate date;

    private String content;

    @Builder
    public DailyNoteRequest(LocalDate date, String content) {
        this.date = date;
        this.content = content;
    }
}
