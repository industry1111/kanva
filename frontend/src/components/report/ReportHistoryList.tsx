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
      <div className="bg-white rounded-xl p-4 shadow-sm flex-1 min-h-0 flex flex-col">
        <h3 className="text-[13px] font-semibold text-text mb-2 pb-2 border-b border-border">이전 리포트</h3>
        <div className="flex flex-col gap-2 text-text-secondary text-[13px]">
          <div className="skeleton-line" />
          <div className="skeleton-line" />
          <div className="skeleton-line" />
        </div>
      </div>
    );
  }

  if (reports.length === 0) {
    return (
      <div className="bg-white rounded-xl p-4 shadow-sm flex-1 min-h-0 flex flex-col">
        <h3 className="text-[13px] font-semibold text-text mb-2 pb-2 border-b border-border">이전 리포트</h3>
        <p className="flex flex-col gap-2 text-text-secondary text-[13px]">아직 생성된 리포트가 없습니다.</p>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-xl p-4 shadow-sm flex-1 min-h-0 flex flex-col">
      <h3 className="text-[13px] font-semibold text-text mb-2 pb-2 border-b border-border">이전 리포트</h3>
      <ul className="list-none p-0 m-0 flex flex-col gap-1.5 overflow-y-auto max-h-[220px]">
        {reports.map((report) => (
          <li
            key={report.id}
            className={`flex items-center justify-between py-1 px-2 border rounded-lg cursor-pointer transition-colors hover:bg-bg group ${
              selectedReportId === report.id
                ? 'bg-bg border-primary'
                : 'border-border'
            }`}
            onClick={() => onSelectReport(report)}
          >
            <span className="text-xs text-text">
              {formatPeriod(report.periodStart, report.periodEnd)}
            </span>
            <div className="flex items-center gap-1.5">
              <span className="text-xs font-semibold text-primary">{report.completionRate ?? 0}%</span>
              {onDeleteReport && (
                <button
                  className="flex items-center justify-center w-5 h-5 border-none bg-transparent text-text-secondary text-base font-semibold cursor-pointer rounded p-0 leading-none transition-all opacity-0 group-hover:opacity-100 hover:text-danger hover:bg-bg"
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
