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

export interface User {
  id: number;
  email: string;
  name: string;
  role: Role;
  createdAt: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface SignUpRequest {
  email: string;
  password: string;
  name: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  user: User;
}

// Task types
export type TaskStatus = 'PENDING' | 'IN_PROGRESS' | 'COMPLETED';

export interface Task {
  id: number;
  dailyNoteId: number;
  title: string;
  description?: string;
  dueDate?: string;
  status: TaskStatus;
  position: number;
  overdue: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface TaskRequest {
  title: string;
  description?: string;
  dueDate?: string;
  status?: TaskStatus;
  position?: number;
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
