import type {
  ApiResponse,
  LoginRequest,
  LoginResponse,
  SignUpRequest,
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

const API_BASE_URL = 'http://localhost:8080/api';

// TODO: 임시 개발용 - 인증 없이 API 테스트
const DEV_SKIP_AUTH = true;

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

  // TODO: 임시 개발용 - 401 리다이렉트 스킵
  if (!DEV_SKIP_AUTH && response.status === 401) {
    clearTokens();
    window.location.href = '/login';
    throw new Error('Unauthorized');
  }

  const data = await response.json();
  return data as ApiResponse<T>;
}

// Auth API
export const authApi = {
  login: async (request: LoginRequest): Promise<ApiResponse<LoginResponse>> => {
    const response = await fetch(`${API_BASE_URL}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(request),
    });
    return response.json();
  },

  signUp: async (request: SignUpRequest): Promise<ApiResponse<User>> => {
    const response = await fetch(`${API_BASE_URL}/auth/signup`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(request),
    });
    return response.json();
  },

  getCurrentUser: async (): Promise<ApiResponse<User>> => {
    return fetchWithAuth('/auth/me');
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

// Dashboard API
export const dashboardApi = {
  get: async (month: string): Promise<ApiResponse<DashboardResponse>> => {
    return fetchWithAuth(`/dashboard?month=${month}`);
  },
};
