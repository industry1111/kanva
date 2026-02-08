import type {
  ApiResponse,
  LoginResponse,
  OAuthLoginUrlResponse,
  OAuthCallbackRequest,
  User,
  Task,
  TaskRequest,
  TaskStatusUpdateRequest,
  TaskPositionUpdateRequest,
  DailyNote,
  DailyNoteRequest,
  DailyNoteSummary,
  DashboardResponse,
} from '../types/api';
import type {
  AIReportSummary,
  AIReport,
  AIReportDetail,
  AIReportRequest,
  ReportFeedbackRequest,
  PageResponse,
} from '../types/report';

const API_BASE_URL = import.meta.env.PROD ? '/api' : 'http://localhost:8080/api';

// Token management
const TOKEN_KEY = 'kanva_access_token';
const REFRESH_TOKEN_KEY = 'kanva_refresh_token';

export const getToken = (): string | null => localStorage.getItem(TOKEN_KEY);
export const getRefreshToken = (): string | null => localStorage.getItem(REFRESH_TOKEN_KEY);

export const setTokens = (accessToken: string, refreshToken: string): void => {
  localStorage.setItem(TOKEN_KEY, accessToken);
  localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
};

export const clearTokens = (): void => {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
};

// Base fetch function with auth header
async function fetchWithAuth<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<ApiResponse<T>> {
  const token = getToken();

  const headers: HeadersInit = {
    'Content-Type': 'application/json',
    ...options.headers,
  };

  if (token) {
    (headers as Record<string, string>)['Authorization'] = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    ...options,
    headers,
  });

  if (response.status === 401) {
    clearTokens();
    window.location.href = '/';
    throw new Error('Unauthorized');
  }

  const data = await response.json();
  return data as ApiResponse<T>;
}

// Auth API
export const authApi = {
  getOAuthLoginUrl: async (provider: string): Promise<ApiResponse<OAuthLoginUrlResponse>> => {
    const response = await fetch(`${API_BASE_URL}/auth/oauth/${provider}/login-url`);
    return response.json();
  },

  oauthCallback: async (provider: string, request: OAuthCallbackRequest): Promise<ApiResponse<LoginResponse>> => {
    const response = await fetch(`${API_BASE_URL}/auth/oauth/${provider}/callback`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(request),
    });
    return response.json();
  },

  getCurrentUser: async (): Promise<ApiResponse<User>> => {
    return fetchWithAuth('/auth/me');
  },

  devLogin: async (): Promise<ApiResponse<LoginResponse>> => {
    const response = await fetch(`${API_BASE_URL}/auth/dev-login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
    });
    return response.json();
  },
};

// DailyNote API
export const dailyNoteApi = {
  getByDate: async (date: string): Promise<ApiResponse<DailyNote>> => {
    return fetchWithAuth(`/daily-notes/${date}`);
  },

  update: async (date: string, request: DailyNoteRequest): Promise<ApiResponse<DailyNote>> => {
    return fetchWithAuth(`/daily-notes/${date}`, {
      method: 'PUT',
      body: JSON.stringify(request),
    });
  },

  delete: async (date: string): Promise<void> => {
    await fetchWithAuth(`/daily-notes/${date}`, {
      method: 'DELETE',
    });
  },

  getMonthly: async (month: string): Promise<ApiResponse<DailyNoteSummary[]>> => {
    return fetchWithAuth(`/daily-notes/calendar?month=${month}`);
  },
};

// Task API
export const taskApi = {
  getByDate: async (date: string): Promise<ApiResponse<Task[]>> => {
    return fetchWithAuth(`/tasks?date=${date}`);
  },

  getById: async (taskId: number): Promise<ApiResponse<Task>> => {
    return fetchWithAuth(`/tasks/${taskId}`);
  },

  create: async (date: string, request: TaskRequest): Promise<ApiResponse<Task>> => {
    return fetchWithAuth(`/tasks?date=${date}`, {
      method: 'POST',
      body: JSON.stringify(request),
    });
  },

  update: async (taskId: number, request: TaskRequest): Promise<ApiResponse<Task>> => {
    return fetchWithAuth(`/tasks/${taskId}`, {
      method: 'PUT',
      body: JSON.stringify(request),
    });
  },

  updateStatus: async (taskId: number, request: TaskStatusUpdateRequest): Promise<ApiResponse<Task>> => {
    return fetchWithAuth(`/tasks/${taskId}/status`, {
      method: 'PATCH',
      body: JSON.stringify(request),
    });
  },

  toggle: async (taskId: number): Promise<ApiResponse<Task>> => {
    return fetchWithAuth(`/tasks/${taskId}/toggle`, {
      method: 'PATCH',
    });
  },

  delete: async (taskId: number): Promise<void> => {
    await fetchWithAuth(`/tasks/${taskId}`, {
      method: 'DELETE',
    });
  },

  updatePositions: async (date: string, request: TaskPositionUpdateRequest): Promise<ApiResponse<Task[]>> => {
    return fetchWithAuth(`/tasks/positions?date=${date}`, {
      method: 'PUT',
      body: JSON.stringify(request),
    });
  },

  getOverdue: async (): Promise<ApiResponse<Task[]>> => {
    return fetchWithAuth('/tasks/overdue');
  },
};

// TaskSeries API
export const taskSeriesApi = {
  excludeDate: async (taskSeriesId: number, date: string): Promise<ApiResponse<void>> => {
    return fetchWithAuth(`/task-series/${taskSeriesId}/exclude`, {
      method: 'POST',
      body: JSON.stringify({ date }),
    });
  },

  stopSeries: async (taskSeriesId: number, stopDate: string): Promise<ApiResponse<void>> => {
    return fetchWithAuth(`/task-series/${taskSeriesId}/stop`, {
      method: 'POST',
      body: JSON.stringify({ stopDate }),
    });
  },
};

// Dashboard API
export const dashboardApi = {
  get: async (month: string): Promise<ApiResponse<DashboardResponse>> => {
    return fetchWithAuth(`/dashboard?month=${month}`);
  },
};

// Report API
export const reportApi = {
  getSummary: async (): Promise<ApiResponse<AIReportSummary>> => {
    return fetchWithAuth('/reports/summary');
  },

  generate: async (request: AIReportRequest): Promise<ApiResponse<AIReport>> => {
    return fetchWithAuth('/reports', {
      method: 'POST',
      body: JSON.stringify(request),
    });
  },

  getDetail: async (reportId: number): Promise<ApiResponse<AIReportDetail>> => {
    return fetchWithAuth(`/reports/${reportId}`);
  },

  getHistory: async (page: number = 0, size: number = 10): Promise<ApiResponse<PageResponse<AIReport>>> => {
    return fetchWithAuth(`/reports?page=${page}&size=${size}`);
  },

  submitFeedback: async (reportId: number, request: ReportFeedbackRequest): Promise<ApiResponse<void>> => {
    return fetchWithAuth(`/reports/${reportId}/feedback`, {
      method: 'POST',
      body: JSON.stringify(request),
    });
  },
};
