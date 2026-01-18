package com.kanva.service.impl;

import com.kanva.domain.dailynote.DailyNote;
import com.kanva.domain.dailynote.DailyNoteRepository;
import com.kanva.dto.dailynote.*;
import com.kanva.service.DailyNoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DailyNoteServiceImpl implements DailyNoteService {

    private final DailyNoteRepository dailyNoteRepository;

    @Override
    @Transactional
    public DailyNoteDetailResponse getOrCreateDailyNote(Long userId, LocalDate date) {
        DailyNote dailyNote = dailyNoteRepository.findByUserIdAndDate(userId, date)
                .orElseGet(() -> createEmptyDailyNote(userId, date));

        return DailyNoteDetailResponse.from(dailyNote);
    }

    @Override
    @Transactional
    public DailyNoteResponse updateDailyNote(Long userId, LocalDate date, DailyNoteRequest request) {
        DailyNote dailyNote = dailyNoteRepository.findByUserIdAndDate(userId, date)
                .orElseGet(() -> createEmptyDailyNote(userId, date));

        dailyNote.updateContent(request.getContent());

        return DailyNoteResponse.from(dailyNote);
    }

    @Override
    @Transactional
    public void deleteDailyNote(Long userId, LocalDate date) {
        dailyNoteRepository.findByUserIdAndDate(userId, date)
                .ifPresent(dailyNoteRepository::delete);
    }

    @Override
    public List<DailyNoteSummaryResponse> getMonthlyNotes(Long userId, YearMonth yearMonth) {
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        return dailyNoteRepository.findByUserIdAndDateRange(userId, startDate, endDate)
                .stream()
                .map(DailyNoteSummaryResponse::from)
                .toList();
    }

    private DailyNote createEmptyDailyNote(Long userId, LocalDate date) {
        DailyNote dailyNote = DailyNote.builder()
                .userId(userId)
                .date(date)
                .content(null)
                .build();

        return dailyNoteRepository.save(dailyNote);
    }
}
