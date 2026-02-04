// AI Report types
export type ReportPeriodType = 'WEEKLY' | 'MONTHLY' | 'CUSTOM';
export type ReportStatus = 'GENERATING' | 'COMPLETED' | 'FAILED';
export type ReportFeedback = 'HELPFUL' | 'NOT_HELPFUL' | 'NEUTRAL';

export interface AIReportSummary {
  id?: number;
  periodType?: ReportPeriodType;
  periodStart?: string;
  periodEnd?: string;
  completionRate?: number;
  trend?: string;
  summary?: string;
  hasReport: boolean;
}

export interface AIReport {
  id: number;
  periodType: ReportPeriodType;
  periodStart: string;
  periodEnd: string;
  status: ReportStatus;
  totalTasks?: number;
  completedTasks?: number;
  completionRate?: number;
  trend?: string;
  summary?: string;
  feedback?: ReportFeedback;
  createdAt: string;
}

export interface AIReportDetail extends AIReport {
  insights?: string;
  recommendations?: string;
  errorMessage?: string;
}

export interface AIReportRequest {
  periodType: ReportPeriodType;
  periodStart?: string;
  periodEnd?: string;
}

export interface ReportFeedbackRequest {
  feedback: ReportFeedback;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}
