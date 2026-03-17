import { useState, useEffect, useCallback } from 'react';
import PeriodSelector from '../components/report/PeriodSelector';
import ToneSelector from '../components/report/ToneSelector';
import ReportHistoryList from '../components/report/ReportHistoryList';
import InsightCard from '../components/report/InsightCard';
import RecommendationCard from '../components/report/RecommendationCard';
import FeedbackButton from '../components/report/FeedbackButton';
import { useAuth } from '../contexts/AuthContext';
import { reportApi } from '../services/api';
import type {
  ReportTone,
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
      return '데이터 수집 중';
    default:
      return '-';
  }
}

function getTrendColor(trend?: string): string {
  switch (trend) {
    case 'UP':
      return '#22C55E';
    case 'DOWN':
      return '#ef4444';
    default:
      return '#64748B';
  }
}

function getDefaultStartDate(): string {
  const today = new Date();
  const day = today.getDay();
  const diff = day === 0 ? 6 : day - 1; // 월요일 기준
  const monday = new Date(today);
  monday.setDate(today.getDate() - diff);
  return monday.toISOString().split('T')[0];
}

function getToday(): string {
  return new Date().toISOString().split('T')[0];
}

export default function AIReportPage() {
  const _auth = useAuth();
  const [startDate, setStartDate] = useState(getDefaultStartDate);
  const [endDate, setEndDate] = useState(getToday);
  const [selectedTone, setSelectedTone] = useState<ReportTone>('ENCOURAGING');
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
      const response = await reportApi.generate({
        periodType: 'CUSTOM',
        periodStart: startDate,
        periodEnd: endDate,
        tone: selectedTone,
      });
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

  const handleDeleteReport = async (reportId: number) => {
    try {
      const response = await reportApi.delete(reportId);
      if (response.success) {
        // 현재 보고 있던 리포트를 삭제한 경우
        if (currentReport?.id === reportId) {
          setCurrentReport(null);
        }
        // 히스토리에서 제거
        const updatedHistory = reportHistory.filter((r) => r.id !== reportId);
        setReportHistory(updatedHistory);
        // 삭제 후 다음 리포트 자동 선택
        if (currentReport?.id === reportId && updatedHistory.length > 0) {
          loadReportDetail(updatedHistory[0].id);
        }
      }
    } catch (error) {
      console.error('Failed to delete report:', error);
    }
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
        <div className="flex flex-col items-center justify-center py-16 gap-3 text-text-secondary">
          <div
            className="w-8 h-8 rounded-full"
            style={{ border: '3px solid var(--color-border)', borderTopColor: 'var(--color-primary)', animation: 'spin 1s linear infinite' }}
          />
          <span className="text-sm">리포트 로딩 중...</span>
        </div>
      );
    }

    if (!currentReport) {
      return (
        <div className="flex flex-col items-center justify-center py-16 text-center max-w-md mx-auto">
          <div className="text-2xl mb-2">📊</div>
          <h3 className="text-base font-semibold text-text mb-1.5">생성된 리포트가 없습니다</h3>
          <p className="text-xs text-text-secondary leading-relaxed m-0">
            기간을 선택하고 새 리포트를 생성해보세요.
            <br />
            AI가 당신의 생산성을 분석해드립니다.
          </p>
        </div>
      );
    }

    return (
      <div className="flex flex-col gap-4">
        <div className="flex items-start justify-between pb-3 border-b border-border">
          <div className="flex flex-col gap-0.5">
            <h2 className="text-base font-semibold text-text m-0">
              {formatDate(currentReport.periodStart)} - {formatDate(currentReport.periodEnd)}
            </h2>
            <span className="text-xs text-text-secondary">
              {currentReport.periodType === 'WEEKLY'
                ? '주간 리포트'
                : currentReport.periodType === 'MONTHLY'
                  ? '월간 리포트'
                  : '기간 리포트'}
            </span>
          </div>
        </div>

        <div className="grid grid-cols-4 gap-2">
          <div className="flex flex-col items-center p-2.5 bg-bg rounded-lg gap-0.5">
            <span className="text-xl font-bold text-text">{currentReport.totalTasks ?? 0}</span>
            <span className="text-[11px] text-text-secondary">전체 할 일</span>
          </div>
          <div className="flex flex-col items-center p-2.5 bg-bg rounded-lg gap-0.5">
            <span className="text-xl font-bold text-text">{currentReport.completedTasks ?? 0}</span>
            <span className="text-[11px] text-text-secondary">완료</span>
          </div>
          <div className="flex flex-col items-center p-2.5 bg-bg rounded-lg gap-0.5">
            <span className="text-xl font-bold text-primary">{currentReport.completionRate ?? 0}%</span>
            <span className="text-[11px] text-text-secondary">완료율</span>
          </div>
          <div className="flex flex-col items-center p-2.5 bg-bg rounded-lg gap-0.5">
            <span
              className="text-xl font-bold"
              style={{ color: getTrendColor(currentReport.trend) }}
            >
              {getTrendText(currentReport.trend)}
            </span>
            <span className="text-[11px] text-text-secondary">트렌드</span>
            {currentReport.trend === 'NEW' && (
              <span className="text-[10px] text-text-secondary mt-0.5">다음 리포트부터 비교됩니다</span>
            )}
          </div>
        </div>

        <div className="flex flex-col gap-1.5">
          <h3 className="text-[13px] font-semibold text-text m-0">요약</h3>
          <p className="text-[13px] text-text-secondary leading-relaxed m-0">{currentReport.summary}</p>
        </div>

        <div className="grid grid-cols-2 gap-3">
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
    <div className="bg-bg p-4">
      <main className="grid grid-cols-4 gap-6 max-w-7xl mx-auto">
        <aside className="col-span-1 flex flex-col gap-4">
          <PeriodSelector
            startDate={startDate}
            endDate={endDate}
            onChangeStart={setStartDate}
            onChangeEnd={setEndDate}
            disabled={isGenerating}
          />
          <ToneSelector
            selectedTone={selectedTone}
            onSelectTone={setSelectedTone}
            disabled={isGenerating}
          />
          <button
            className="flex items-center justify-center gap-2 w-full py-2 bg-primary text-white border-none rounded-lg text-[13px] font-semibold cursor-pointer transition-colors hover:bg-primary-hover disabled:opacity-70 disabled:cursor-not-allowed"
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
            onDeleteReport={handleDeleteReport}
            isLoading={historyLoading}
          />
        </aside>

        <section className="col-span-3 min-w-0 bg-white rounded-xl p-4 shadow-sm">{renderReportContent()}</section>
      </main>
    </div>
  );
}
