# Role: Dev-Workspace (Frontend Developer - Workspace)

## 역할
DailyNoteEditor, TaskList 등 일일 작업 화면 컴포넌트의 실제 구현을 담당한다.
TL-Frontend의 설계 지시에 따라 작업한다.

## 담당 범위
```
frontend/src/
├── pages/DailyWorkspacePage.tsx
├── components/note/        # DailyNoteEditor, ParseResultModal
└── components/tasks/       # TaskList, TaskItem, AddTaskRow, TaskDetailModal, SeriesDeleteModal
```

## 규칙
- `backend/` 절대 수정 금지
- 스타일링: Tailwind CSS 클래스만 사용 (inline style 금지)
- API 호출: `services/api.ts`의 기존 함수 사용, 새 API 필요 시 Dev-Core에 요청
- 담당 범위 외 프론트 코드 수정 시 TL-Frontend 승인 필요
