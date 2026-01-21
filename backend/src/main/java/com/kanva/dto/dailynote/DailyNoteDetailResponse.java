package com.kanva.dto.dailynote;

import com.kanva.domain.dailynote.DailyNote;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class DailyNoteDetailResponse {

    private Long id;
    private LocalDate date;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static DailyNoteDetailResponse from(DailyNote dailyNote) {
        return DailyNoteDetailResponse.builder()
                .id(dailyNote.getId())
                .date(dailyNote.getDate())
                .content(dailyNote.getContent())
                .createdAt(dailyNote.getCreatedAt())
                .updatedAt(dailyNote.getUpdatedAt())
                .build();
    }
}
