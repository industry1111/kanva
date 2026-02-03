package com.kanva.service.notification;

import com.kanva.domain.notification.NotificationSlot;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * 알림 메시지 빌더
 */
@Component
public class NotificationMessageBuilder {

    private static final int MAX_DISPLAY_TASKS = 10;
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy년 M월 d일 (E)", Locale.KOREAN);

    /**
     * 슬롯에 따른 알림 메시지 생성
     */
    public String buildMessage(NotificationSlot slot, LocalDate date, List<String> taskTitles) {
        return switch (slot) {
            case MORNING -> buildMorningMessage(date, taskTitles);
            case EVENING -> buildEveningMessage(date, taskTitles);
        };
    }

    /**
     * 오전 알림: 오늘 할 일 목록
     */
    private String buildMorningMessage(LocalDate date, List<String> taskTitles) {
        String dateStr = date.format(DATE_FORMATTER);
        StringBuilder msg = new StringBuilder();
        msg.append("📅 ").append(dateStr).append(" 오늘의 할 일\n\n");

        if (taskTitles.isEmpty()) {
            msg.append("등록된 Task가 없습니다.");
        } else {
            appendTaskList(msg, taskTitles);
            msg.append("\n총 ").append(taskTitles.size()).append("개");
        }

        return msg.toString();
    }

    /**
     * 오후 알림: 미완료 Task 리마인더
     */
    private String buildEveningMessage(LocalDate date, List<String> incompleteTitles) {
        String dateStr = date.format(DATE_FORMATTER);
        StringBuilder msg = new StringBuilder();
        msg.append("⏰ ").append(dateStr).append(" 미완료 Task 리마인더\n\n");

        if (incompleteTitles.isEmpty()) {
            msg.append("오늘 할 일을 모두 완료했습니다! 🎉");
        } else {
            for (String title : incompleteTitles) {
                msg.append("☐ ").append(title).append("\n");
            }
            msg.append("\n아직 ").append(incompleteTitles.size()).append("개의 Task가 남아있습니다.");
        }

        return msg.toString();
    }

    private void appendTaskList(StringBuilder msg, List<String> titles) {
        int displayCount = Math.min(titles.size(), MAX_DISPLAY_TASKS);
        for (int i = 0; i < displayCount; i++) {
            msg.append("• ").append(titles.get(i)).append("\n");
        }

        int remaining = titles.size() - displayCount;
        if (remaining > 0) {
            msg.append("  ...외 ").append(remaining).append("개\n");
        }
    }
}
