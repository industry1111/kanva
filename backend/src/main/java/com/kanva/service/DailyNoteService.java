package com.kanva.service;

import com.kanva.dto.dailynote.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public interface DailyNoteService {

    DailyNoteDetailResponse getOrCreateDailyNote(Long userId, LocalDate date);

    DailyNoteResponse updateDailyNote(Long userId, LocalDate date, DailyNoteRequest request);

    void deleteDailyNote(Long userId, LocalDate date);

    List<DailyNoteSummaryResponse> getMonthlyNotes(Long userId, YearMonth yearMonth);
}
