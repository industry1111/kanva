import { useState, useEffect, useCallback, useRef } from 'react';
import MonthSelector from '../components/dashboard/MonthSelector';
import TaskStats from '../components/dashboard/TaskStats';
import ProductivityChart from '../components/dashboard/ProductivityChart';
import AIReportCard from '../components/dashboard/AIReportCard';
import { useAuth } from '../contexts/AuthContext';
import { dashboardApi } from '../services/api';
import type { DashboardStats, DailyStat } from '../types/api';

interface DashboardPageProps {
  onNavigateToReport?: () => void;
}

function getCurrentMonth(): string {
  const today = new Date();
  return `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}`;
}

export default function DashboardPage({ onNavigateToReport }: DashboardPageProps) {
  const { user, logout } = useAuth();
  const [selectedMonth, setSelectedMonth] = useState(getCurrentMonth());
  const [taskStats, setTaskStats] = useState<DashboardStats>({
    completed: 0,
    inProgress: 0,
    pending: 0,
    overdue: 0,
  });
  const [dailyStats, setDailyStats] = useState<DailyStat[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isDataLoading, setIsDataLoading] = useState(false);
  const isInitialLoad = useRef(true);

  const loadDashboardData = useCallback(async (month: string, showFullLoading: boolean) => {
    if (showFullLoading) {
      setIsLoading(true);
    } else {
      setIsDataLoading(true);
    }

    try {
      // 단일 API 호출로 모든 데이터 조회
      const response = await dashboardApi.get(month);

      if (response.success) {
        setTaskStats(response.data.stats);
        setDailyStats(response.data.dailyStats);
      }
    } catch (error) {
      console.error('Failed to load dashboard data:', error);
    } finally {
      setIsLoading(false);
      setIsDataLoading(false);
    }
  }, []);

  useEffect(() => {
    loadDashboardData(selectedMonth, isInitialLoad.current);
    isInitialLoad.current = false;
  }, [selectedMonth, loadDashboardData]);

  const handleMonthChange = (month: string) => {
    if (month !== selectedMonth) {
      setSelectedMonth(month);
    }
  };

  // DailyStat을 ProductivityChart용 형식으로 변환
  const productivityData = dailyStats.map((stat) => ({
    date: stat.date,
    completed: stat.completedCount,
    total: stat.totalCount,
  }));

  if (isLoading) {
    return (
      <div className="dashboard-container">
        <div className="dashboard-loading">
          <div className="dashboard-spinner" />
          <span>대시보드 로딩 중...</span>
        </div>
      </div>
    );
  }

  return (
    <div className="dashboard-container">
      <header className="dashboard-header">
        <div className="logo">
          <span className="logo-icon">K</span>
          <span className="logo-text">Kanva</span>
        </div>
        <h1 className="dashboard-title">Dashboard Overview</h1>
        <div className="dashboard-user-info">
          <span className="dashboard-user-name">{user?.name}</span>
          <button onClick={logout} className="dashboard-logout-btn">
            로그아웃
          </button>
        </div>
      </header>

      <main className="dashboard-main">
        <div className={`dashboard-grid ${isDataLoading ? 'loading' : ''}`}>
          <MonthSelector
            selectedMonth={selectedMonth}
            onSelectMonth={handleMonthChange}
          />
          <TaskStats
            completed={taskStats.completed}
            pending={taskStats.pending}
            inProgress={taskStats.inProgress}
          />
          <ProductivityChart
            data={productivityData}
            selectedMonth={selectedMonth}
          />
          <AIReportCard onViewDetails={onNavigateToReport || (() => {})} />
        </div>
        {isDataLoading && (
          <div className="dashboard-data-loading">
            <div className="dashboard-spinner-small" />
          </div>
        )}
      </main>
    </div>
  );
}
