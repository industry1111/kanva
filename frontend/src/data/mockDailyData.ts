export interface Task {
  id: number;
  title: string;
  completed: boolean;
}

export interface DailyData {
  date: string;
  note: string;
  tasks: Task[];
}

const STORAGE_KEY = 'kanva_daily_data';

const initialData: Record<string, DailyData> = {
  '2025-01-21': {
    date: '2025-01-21',
    note: '# 오늘의 메모\n\n- 프론트엔드 개발 시작\n- 회의 준비',
    tasks: [
      { id: 1, title: 'React 컴포넌트 구현', completed: false },
      { id: 2, title: '코드 리뷰', completed: true },
      { id: 3, title: '문서 작성', completed: false },
    ],
  },
  '2025-01-20': {
    date: '2025-01-20',
    note: '어제의 메모입니다.',
    tasks: [
      { id: 1, title: '백엔드 API 테스트', completed: true },
    ],
  },
};

export function loadDailyData(): Record<string, DailyData> {
  const stored = localStorage.getItem(STORAGE_KEY);
  if (stored) {
    return JSON.parse(stored);
  }
  return { ...initialData };
}

export function saveDailyData(data: Record<string, DailyData>): void {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(data));
}

export function getDefaultDailyData(date: string): DailyData {
  return {
    date,
    note: '',
    tasks: [],
  };
}

// 기존 호환성 유지
export const mockDailyData = initialData;
