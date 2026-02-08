import type { AIReport } from '../../types/report';

interface ReportHistoryListProps {
  reports: AIReport[];
  selectedReportId?: number;
  onSelectReport: (report: AIReport) => void;
  onDeleteReport?: (reportId: number) => void;
  isLoading?: boolean;
}

function formatDate(dateStr: string): string {
  const date = new Date(dateStr);
  return `${date.getMonth() + 1}/${date.getDate()}`;
}

function formatPeriod(start: string, end: string): string {
  return `${formatDate(start)} - ${formatDate(end)}`;
}

export default function ReportHistoryList({
  reports,
  selectedReportId,
  onSelectReport,
  onDeleteReport,
  isLoading = false,
}: ReportHistoryListProps) {
  const handleDelete = (e: React.MouseEvent, reportId: number) => {
    e.stopPropagation();
    if (onDeleteReport) {
      onDeleteReport(reportId);
    }
  };

  if (isLoading) {
    return (
      <div className="report-history">
        <h3 className="report-history-title">이전 리포트</h3>
        <div className="report-history-loading">
          <div className="skeleton-line" />
          <div className="skeleton-line" />
          <div className="skeleton-line" />
        </div>
      </div>
    );
  }

  if (reports.length === 0) {
    return (
      <div className="report-history">
        <h3 className="report-history-title">이전 리포트</h3>
        <p className="report-history-empty">아직 생성된 리포트가 없습니다.</p>
      </div>
    );
  }

  return (
    <div className="report-history">
      <h3 className="report-history-title">이전 리포트</h3>
      <ul className="report-history-list">
        {reports.map((report) => (
          <li
            key={report.id}
            className={`report-history-item ${selectedReportId === report.id ? 'active' : ''}`}
            onClick={() => onSelectReport(report)}
          >
            <span className="report-history-period">
              {formatPeriod(report.periodStart, report.periodEnd)}
            </span>
            <div className="report-history-actions">
              <span className="report-history-rate">{report.completionRate ?? 0}%</span>
              {onDeleteReport && (
                <button
                  className="report-delete-btn"
                  onClick={(e) => handleDelete(e, report.id)}
                  title="삭제"
                >
                  ×
                </button>
              )}
            </div>
          </li>
        ))}
      </ul>
    </div>
  );
}
