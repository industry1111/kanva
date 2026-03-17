package com.kanva.dto.calendar;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CalendarResponse {

    private List<CalendarTask> tasks;

    @Getter
    @Builder
    public static class CalendarTask {
        private Long id;
        private String title;
        private String date;
        private String status;
        private String type;
        private String category;
        private Long seriesId;
    }
}
