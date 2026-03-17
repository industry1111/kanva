import { useState, useEffect, useCallback, useRef } from 'react';
import MonthSelector from '../components/dashboard/MonthSelector';
import MonthlyCalendar from '../components/dashboard/MonthlyCalendar';
import { useAuth } from '../contexts/AuthContext';
import { dashboardApi } from '../services/api';
import type { DashboardStats, DailyStat } from '../types/api';

function getCurrentMonth(): string {
  const today = new Date();
  return `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}`;
}

export default function DashboardPage() {
  const _auth = useAuth();
  const [selectedMonth, setSelectedMonth] = useState(getCurrentMonth());
  const [taskStats, setTaskStats] = useState<DashboardStats>({
    completed: 0,
    inProgress: 0,
    pending: 0,
    overdue: 0,
  });
  const [_dailyStats, setDailyStats] = useState<DailyStat[]>([]);
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

  if (isLoading) {
    return (
      <div className="bg-bg p-4">
        <div className="flex flex-col items-center justify-center py-16 gap-4 text-text-secondary">
          <div
            className="w-8 h-8 rounded-full"
            style={{
              border: '3px solid #E2E8F0',
              borderTopColor: '#0F9D9A',
              animation: 'spin 0.8s linear infinite',
            }}
          />
          <span>대시보드 로딩 중...</span>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-bg p-4">
      <main className="max-w-5xl mx-auto">
        <div className="flex items-center justify-between">
          <MonthSelector
            selectedMonth={selectedMonth}
            onSelectMonth={handleMonthChange}
          />
          <div className="flex items-center gap-3">
            <span className="flex items-center gap-1 text-xs font-medium text-text-secondary">
              <span className="w-1.5 h-1.5 rounded-full bg-primary" />
              완료 {taskStats.completed}
            </span>
            <span className="flex items-center gap-1 text-xs font-medium text-text-secondary">
              <span className="w-1.5 h-1.5 rounded-full bg-border" />
              미완료 {taskStats.pending + taskStats.inProgress}
            </span>
          </div>
        </div>
        <div className={`flex flex-col gap-4 ${isDataLoading ? 'opacity-60 pointer-events-none' : ''}`}>
          <MonthlyCalendar
            selectedMonth={selectedMonth}
          />
        </div>
        {isDataLoading && (
          <div className="flex justify-center py-3">
            <div
              className="w-5 h-5 rounded-full"
              style={{
                border: '2px solid #E2E8F0',
                borderTopColor: '#0F9D9A',
                animation: 'spin 0.8s linear infinite',
              }}
            />
          </div>
        )}
      </main>
    </div>
  );
}
