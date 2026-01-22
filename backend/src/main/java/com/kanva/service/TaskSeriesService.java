package com.kanva.service;

import com.kanva.dto.taskseries.TaskSeriesRequest;
import com.kanva.dto.taskseries.TaskSeriesResponse;

import java.util.List;

public interface TaskSeriesService {

    TaskSeriesResponse createSeries(Long userId, TaskSeriesRequest request);

    List<TaskSeriesResponse> getUserSeries(Long userId);

    List<TaskSeriesResponse> getUserActiveSeries(Long userId);

    void generateTodayTasks();
}
