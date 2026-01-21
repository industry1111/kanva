package com.kanva.dto.dailynote;

import com.kanva.domain.dailynote.DailyNote;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class DailyNoteSummaryResponse {

    private LocalDate date;
    private boolean hasContent;

    public static DailyNoteSummaryResponse from(DailyNote dailyNote) {
        return DailyNoteSummaryResponse.builder()
                .date(dailyNote.getDate())
                .hasContent(dailyNote.getContent() != null && !dailyNote.getContent().isEmpty())
                .build();
    }
}
