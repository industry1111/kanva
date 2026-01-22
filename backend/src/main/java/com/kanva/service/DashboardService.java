package com.kanva.service;

import com.kanva.dto.dashboard.DashboardResponse;

import java.time.YearMonth;

public interface DashboardService {

    DashboardResponse getDashboard(Long userId, YearMonth month);
}
