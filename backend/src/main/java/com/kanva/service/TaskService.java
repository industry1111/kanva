package com.kanva.service;

import com.kanva.dto.task.TaskPositionUpdateRequest;
import com.kanva.dto.task.TaskRequest;
import com.kanva.dto.task.TaskResponse;
import com.kanva.dto.task.TaskStatusUpdateRequest;

import java.time.LocalDate;
import java.util.List;

public interface TaskService {

    /**
     * 특정 날짜의 Task 목록 조회
     */
    List<TaskResponse> getTasksByDate(Long userId, LocalDate date);

    /**
     * Task 단건 조회
     */
    TaskResponse getTask(Long userId, Long taskId);

    /**
     * Task 생성
     */
    TaskResponse createTask(Long userId, LocalDate date, TaskRequest request);

    /**
     * Task 수정
     */
    TaskResponse updateTask(Long userId, Long taskId, TaskRequest request);

    /**
     * Task 상태 변경
     */
    TaskResponse updateTaskStatus(Long userId, Long taskId, TaskStatusUpdateRequest request);

    /**
     * Task 완료 상태 토글 (COMPLETED <-> PENDING)
     */
    TaskResponse toggleTask(Long userId, Long taskId);

    /**
     * Task 삭제
     */
    void deleteTask(Long userId, Long taskId);

    /**
     * Task 순서 변경 (드래그앤드롭)
     */
    List<TaskResponse> updateTaskPositions(Long userId, LocalDate date, TaskPositionUpdateRequest request);

    /**
     * 마감 지난 Task 목록 조회
     */
    List<TaskResponse> getOverdueTasks(Long userId);
}
