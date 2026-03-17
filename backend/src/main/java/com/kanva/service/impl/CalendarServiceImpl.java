package com.kanva.service.impl;

import com.kanva.domain.task.Task;
import com.kanva.domain.task.TaskRepository;
import com.kanva.dto.calendar.CalendarResponse;
import com.kanva.dto.calendar.CalendarResponse.CalendarTask;
import com.kanva.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalendarServiceImpl implements CalendarService {

    private final TaskRepository taskRepository;

    @Override
    public CalendarResponse getMonthlyTasks(Long userId, YearMonth month) {
        LocalDate startDate = month.atDay(1);
        LocalDate endDate = month.atEndOfMonth();

        List<Task> monthTasks = taskRepository.findByUserIdAndDateRange(userId, startDate, endDate);

        List<CalendarTask> calendarTasks = monthTasks.stream()
                .map(this::toCalendarTask)
                .collect(Collectors.toList());

        return CalendarResponse.builder()
                .tasks(calendarTasks)
                .build();
    }

    private CalendarTask toCalendarTask(Task task) {
        return CalendarTask.builder()
                .id(task.getId())
                .title(task.getTitle())
                .date(task.getDailyNote().getDate().toString())
                .status(task.getStatus().name())
                .type(task.getType().name())
                .category(task.getCategory().name())
                .seriesId(task.getSeriesId())
                .build();
    }
}
