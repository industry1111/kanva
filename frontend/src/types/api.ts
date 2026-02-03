// API Response wrapper
export interface ApiResponse<T> {
  success: boolean;
  data: T;
  code: number;
  message: string;
  errors?: FieldError[];
}

export interface FieldError {
  field: string;
  message: string;
}

// User types
export type Role = 'USER' | 'ADMIN';
export type OAuthProvider = 'GITHUB' | 'SLACK';

export interface User {
  id: number;
  email: string;
  name: string;
  role: Role;
  picture?: string;
  oauthProvider?: OAuthProvider;
  createdAt: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  user: User;
}

// OAuth types
export interface OAuthLoginUrlResponse {
  url: string;
  state: string;
}

export interface OAuthCallbackRequest {
  code: string;
  state: string;
}

// Task types
export type TaskStatus = 'PENDING' | 'IN_PROGRESS' | 'COMPLETED';

export interface Task {
  id: number;
  dailyNoteId: number;
  seriesId?: number;
  title: string;
  description?: string;
  status: TaskStatus;
  position: number;
  overdue: boolean;
  repeatDaily: boolean;
  stopOnComplete: boolean;
  endDate?: string;
  createdAt: string;
  updatedAt: string;
}

export interface TaskRequest {
  title: string;
  description?: string;
  status?: TaskStatus;
  position?: number;
  repeatDaily?: boolean;
  stopOnComplete?: boolean;
  endDate?: string;
}

export interface TaskStatusUpdateRequest {
  status: TaskStatus;
}

export interface TaskPositionUpdateRequest {
  taskIds: number[];
}

// DailyNote types
export interface DailyNote {
  id: number;
  date: string;
  content?: string;
  createdAt: string;
  updatedAt: string;
}

export interface DailyNoteRequest {
  date: string;
  content?: string;
}

export interface DailyNoteSummary {
  date: string;
  hasContent: boolean;
}

// Dashboard types
export interface DashboardResponse {
  stats: DashboardStats;
  dailyStats: DailyStat[];
  overdueTasks: TaskSummary[];
  dueSoonTasks: TaskSummary[];
}

export interface DashboardStats {
  completed: number;
  inProgress: number;
  pending: number;
  overdue: number;
}

export interface DailyStat {
  date: string;
  totalCount: number;
  completedCount: number;
}

export interface TaskSummary {
  id: number;
  title: string;
  date: string;
  dueDate: string | null;
  status: string;
}
