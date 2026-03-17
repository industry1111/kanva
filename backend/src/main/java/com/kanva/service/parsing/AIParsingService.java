package com.kanva.service.parsing;

import com.kanva.domain.dailynote.DailyNote;
import com.kanva.domain.task.Task;
import com.kanva.service.report.AIAnalysisService;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

public interface AIParsingService {

    @Getter
    @Builder
    class ParsingContext {
        DailyNote dailyNote;
        List<Task> registeredTasks;
    }

    @Getter
    @Builder
    class ParsingResult {

        String type;        // WORK, SCHEDULE
        String title;
        String description; // 설명
        String dueDate;     // TASK일 때 마감일
        String startDateTime; // SCHEDULE일 때 시작 시간
        String category;    // WORK, EXERCISE, OTHER
        String status;      // PENDING, COMPLETED

        //시리즈는 서비스에서 시작기간과 종료기간으로 생성
    }

    List<ParsingResult> parsing(ParsingContext context);
}
