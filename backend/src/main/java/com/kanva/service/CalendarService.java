package com.kanva.service;

import com.kanva.dto.calendar.CalendarResponse;

import java.time.YearMonth;

public interface CalendarService {

    CalendarResponse getMonthlyTasks(Long userId, YearMonth month);
}
