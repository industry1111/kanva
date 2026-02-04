import { useState, useEffect } from 'react';
import { reportApi } from '../../services/api';
import type { AIReportSummary } from '../../types/report';

interface AIReportCardProps {
  onViewDetails: () => void;
}

export default function AIReportCard({ onViewDetails }: AIReportCardProps) {
  const [summary, setSummary] = useState<AIReportSummary | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isGenerating, setIsGenerating] = useState(false);

  useEffect(() => {
    loadSummary();
  }, []);

  const loadSummary = async () => {
    setIsLoading(true);
    try {
      const response = await reportApi.getSummary();
      if (response.success) {
        setSummary(response.data);
      }
    } catch (error) {
      console.error('Failed to load report summary:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleGenerateReport = async () => {
    setIsGenerating(true);
    try {
      const response = await reportApi.generate({ periodType: 'WEEKLY' });
      if (response.success) {
        // 생성 후 요약 다시 로드
        await loadSummary();
      }
    } catch (error) {
      console.error('Failed to generate report:', error);
    } finally {
      setIsGenerating(false);
    }
  };

  const getTrendIcon = (trend?: string) => {
    switch (trend) {
      case 'UP':
        return '↑';
      case 'DOWN':
        return '↓';
      case 'STABLE':
        return '→';
      default:
        return '•';
    }
  };

  const getTrendColor = (trend?: string) => {
    switch (trend) {
      case 'UP':
        return '#10b981';
      case 'DOWN':
        return '#ef4444';
      default:
        return '#6b7280';
    }
  };

  if (isLoading) {
    return (
      <div className="ai-report-card">
        <div className="ai-report-card-header">
          <span className="ai-icon">✨</span>
          <span className="ai-report-card-title">AI 주간 리포트</span>
        </div>
        <div className="ai-report-skeleton">
          <div className="skeleton-line skeleton-title" />
          <div className="skeleton-line skeleton-text" />
          <div className="skeleton-line skeleton-text short" />
        </div>
      </div>
    );
  }

  if (!summary?.hasReport) {
    return (
      <div className="ai-report-card">
        <div className="ai-report-card-header">
          <span className="ai-icon">✨</span>
          <span className="ai-report-card-title">AI 주간 리포트</span>
          <span className="new-badge">NEW</span>
        </div>
        <div className="ai-report-empty">
          <p className="ai-report-empty-text">
            아직 생성된 리포트가 없습니다.
            <br />
            AI가 당신의 생산성을 분석해드립니다.
          </p>
          <button
            className="ai-report-generate-btn"
            onClick={handleGenerateReport}
            disabled={isGenerating}
          >
            {isGenerating ? (
              <>
                <span className="btn-spinner" />
                생성 중...
              </>
            ) : (
              '리포트 생성하기'
            )}
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="ai-report-card">
      <div className="ai-report-card-header">
        <span className="ai-icon">✨</span>
        <span className="ai-report-card-title">AI 주간 리포트</span>
      </div>
      <div className="ai-report-content">
        <div className="ai-report-stats">
          <div className="ai-report-stat">
            <span className="ai-report-stat-value">{summary.completionRate ?? 0}%</span>
            <span className="ai-report-stat-label">완료율</span>
          </div>
          <div className="ai-report-stat">
            <span
              className="ai-report-stat-value trend"
              style={{ color: getTrendColor(summary.trend) }}
            >
              {getTrendIcon(summary.trend)}
            </span>
            <span className="ai-report-stat-label">트렌드</span>
          </div>
        </div>
        <p className="ai-report-summary">{summary.summary}</p>
        <button className="ai-report-view-btn" onClick={onViewDetails}>
          자세히 보기 →
        </button>
      </div>
    </div>
  );
}
