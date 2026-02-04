import { useState, useEffect, useCallback } from 'react';
import PeriodSelector from '../components/report/PeriodSelector';
import ReportHistoryList from '../components/report/ReportHistoryList';
import InsightCard from '../components/report/InsightCard';
import RecommendationCard from '../components/report/RecommendationCard';
import FeedbackButton from '../components/report/FeedbackButton';
import { useAuth } from '../contexts/AuthContext';
import { reportApi } from '../services/api';
import type {
  ReportPeriodType,
  ReportFeedback,
  AIReport,
  AIReportDetail,
} from '../types/report';

function formatDate(dateStr: string): string {
  const date = new Date(dateStr);
  return `${date.getFullYear()}년 ${date.getMonth() + 1}월 ${date.getDate()}일`;
}

function getTrendText(trend?: string): string {
  switch (trend) {
    case 'UP':
      return '상승 중';
    case 'DOWN':
      return '하락 중';
    case 'STABLE':
      return '유지 중';
    case 'NEW':
      return '첫 리포트';
    default:
      return '-';
  }
}

function getTrendColor(trend?: string): string {
  switch (trend) {
    case 'UP':
      return '#10b981';
    case 'DOWN':
      return '#ef4444';
    default:
      return '#6b7280';
  }
}

export default function AIReportPage() {
  const { user, logout } = useAuth();
  const [selectedPeriod, setSelectedPeriod] = useState<ReportPeriodType>('WEEKLY');
  const [reportHistory, setReportHistory] = useState<AIReport[]>([]);
  const [currentReport, setCurrentReport] = useState<AIReportDetail | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isGenerating, setIsGenerating] = useState(false);
  const [historyLoading, setHistoryLoading] = useState(true);

  const loadReportHistory = useCallback(async () => {
    setHistoryLoading(true);
    try {
      const response = await reportApi.getHistory(0, 20);
      if (response.success) {
        setReportHistory(response.data.content);
        // 첫 번째 리포트 자동 선택
        if (response.data.content.length > 0 && !currentReport) {
          loadReportDetail(response.data.content[0].id);
        } else if (response.data.content.length === 0) {
          setIsLoading(false);
        }
      }
    } catch (error) {
      console.error('Failed to load report history:', error);
    } finally {
      setHistoryLoading(false);
    }
  }, []);

  const loadReportDetail = async (reportId: number) => {
    setIsLoading(true);
    try {
      const response = await reportApi.getDetail(reportId);
      if (response.success) {
        setCurrentReport(response.data);
      }
    } catch (error) {
      console.error('Failed to load report detail:', error);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadReportHistory();
  }, [loadReportHistory]);

  const handleGenerateReport = async () => {
    setIsGenerating(true);
    try {
      const response = await reportApi.generate({ periodType: selectedPeriod });
      if (response.success) {
        // 히스토리 다시 로드
        await loadReportHistory();
        // 새로 생성된 리포트 상세 조회
        loadReportDetail(response.data.id);
      }
    } catch (error) {
      console.error('Failed to generate report:', error);
    } finally {
      setIsGenerating(false);
    }
  };

  const handleSelectReport = (report: AIReport) => {
    loadReportDetail(report.id);
  };

  const handleSubmitFeedback = async (feedback: ReportFeedback) => {
    if (!currentReport) return;

    await reportApi.submitFeedback(currentReport.id, { feedback });
    // 현재 리포트 피드백 상태 업데이트
    setCurrentReport((prev) => (prev ? { ...prev, feedback } : null));
  };

  const renderReportContent = () => {
    if (isLoading) {
      return (
        <div className="report-loading">
          <div className="report-spinner" />
          <span>리포트 로딩 중...</span>
        </div>
      );
    }

    if (!currentReport) {
      return (
        <div className="report-empty">
          <div className="report-empty-icon">📊</div>
          <h3 className="report-empty-title">생성된 리포트가 없습니다</h3>
          <p className="report-empty-text">
            기간을 선택하고 새 리포트를 생성해보세요.
            <br />
            AI가 당신의 생산성을 분석해드립니다.
          </p>
        </div>
      );
    }

    return (
      <div className="report-detail">
        <div className="report-detail-header">
          <div className="report-period-info">
            <h2 className="report-period-title">
              {formatDate(currentReport.periodStart)} - {formatDate(currentReport.periodEnd)}
            </h2>
            <span className="report-period-type">
              {currentReport.periodType === 'WEEKLY' ? '주간 리포트' : '월간 리포트'}
            </span>
          </div>
        </div>

        <div className="report-stats-row">
          <div className="report-stat-card">
            <span className="report-stat-value">{currentReport.totalTasks ?? 0}</span>
            <span className="report-stat-label">전체 할 일</span>
          </div>
          <div className="report-stat-card">
            <span className="report-stat-value">{currentReport.completedTasks ?? 0}</span>
            <span className="report-stat-label">완료</span>
          </div>
          <div className="report-stat-card highlight">
            <span className="report-stat-value">{currentReport.completionRate ?? 0}%</span>
            <span className="report-stat-label">완료율</span>
          </div>
          <div className="report-stat-card">
            <span
              className="report-stat-value"
              style={{ color: getTrendColor(currentReport.trend) }}
            >
              {getTrendText(currentReport.trend)}
            </span>
            <span className="report-stat-label">트렌드</span>
          </div>
        </div>

        <div className="report-summary-section">
          <h3 className="report-section-title">요약</h3>
          <p className="report-summary-text">{currentReport.summary}</p>
        </div>

        <div className="report-insights-grid">
          {currentReport.insights && (
            <InsightCard title="인사이트" content={currentReport.insights} icon="💡" />
          )}
          {currentReport.recommendations && (
            <RecommendationCard content={currentReport.recommendations} />
          )}
        </div>

        <FeedbackButton
          currentFeedback={currentReport.feedback}
          onSubmitFeedback={handleSubmitFeedback}
        />
      </div>
    );
  };

  return (
    <div className="report-container">
      <header className="report-header">
        <div className="logo">
          <span className="logo-icon">K</span>
          <span className="logo-text">Kanva</span>
        </div>
        <h1 className="report-title">AI Report</h1>
        <div className="report-user-info">
          <span className="report-user-name">{user?.name}</span>
          <button onClick={logout} className="report-logout-btn">
            로그아웃
          </button>
        </div>
      </header>

      <main className="report-main">
        <aside className="report-sidebar">
          <PeriodSelector
            selectedPeriod={selectedPeriod}
            onSelectPeriod={setSelectedPeriod}
            disabled={isGenerating}
          />
          <button
            className="report-generate-btn"
            onClick={handleGenerateReport}
            disabled={isGenerating}
          >
            {isGenerating ? (
              <>
                <span className="btn-spinner" />
                생성 중...
              </>
            ) : (
              <>새 리포트 생성</>
            )}
          </button>
          <ReportHistoryList
            reports={reportHistory}
            selectedReportId={currentReport?.id}
            onSelectReport={handleSelectReport}
            isLoading={historyLoading}
          />
        </aside>

        <section className="report-content">{renderReportContent()}</section>
      </main>
    </div>
  );
}
