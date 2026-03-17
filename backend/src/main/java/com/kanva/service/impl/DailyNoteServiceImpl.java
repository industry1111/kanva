package com.kanva.service.impl;

import com.kanva.domain.dailynote.DailyNote;
import com.kanva.domain.dailynote.DailyNoteRepository;
import com.kanva.domain.task.Task;
import com.kanva.domain.task.TaskRepository;
import com.kanva.domain.user.User;
import com.kanva.domain.user.UserRepository;
import com.kanva.dto.dailynote.*;
import com.kanva.exception.UserNotFoundException;
import com.kanva.service.DailyNoteService;
import com.kanva.service.parsing.AIParsingService;
import com.kanva.service.parsing.GeminiAIParsingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.kanva.service.parsing.AIParsingService.ParsingContext;
import com.kanva.service.parsing.AIParsingService.ParsingResult;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DailyNoteServiceImpl implements DailyNoteService {

    private final DailyNoteRepository dailyNoteRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final GeminiAIParsingService geminiAIParsingService;

    @Override
    @Transactional
    public DailyNoteDetailResponse getOrCreateDailyNote(Long userId, LocalDate date) {
        User user = findUserById(userId);

        DailyNote dailyNote = dailyNoteRepository.findByUserAndDate(user, date)
                .orElseGet(() -> createEmptyDailyNote(user, date));

        return DailyNoteDetailResponse.from(dailyNote);
    }

    @Override
    @Transactional
    public DailyNoteResponse updateDailyNote(Long userId, LocalDate date, DailyNoteRequest request) {
        User user = findUserById(userId);

        DailyNote dailyNote = dailyNoteRepository.findByUserAndDate(user, date)
                .orElseGet(() -> createEmptyDailyNote(user, date));

        dailyNote.updateContent(request.getContent());

        return DailyNoteResponse.from(dailyNote);
    }

    @Override
    @Transactional
    public void deleteDailyNote(Long userId, LocalDate date) {
        User user = findUserById(userId);

        dailyNoteRepository.findByUserAndDate(user, date)
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

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private DailyNote createEmptyDailyNote(User user, LocalDate date) {
        DailyNote dailyNote = DailyNote.builder()
                .user(user)
                .date(date)
                .content(null)
                .build();

        return dailyNoteRepository.save(dailyNote);
    }

    public List<ParsingResult> parseDailyNote(Long dailyNoteId) {
        DailyNote dailyNote = dailyNoteRepository.findById(dailyNoteId)
                .orElseThrow(() -> new RuntimeException("DailyNote Not found"));

        List<Task> taskList = taskRepository.findByDailyNoteOrderByPositionAsc(dailyNote);

        ParsingContext parsingContext = ParsingContext.builder()
                .dailyNote(dailyNote)
                .registeredTasks(taskList)
                .build();

        return geminiAIParsingService.parsing(parsingContext);
    }
}
