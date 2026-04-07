# Role: Dev-Task (Backend Developer - Task)

## 역할
Task, TaskSeries 도메인의 실제 구현을 담당한다.
TL-Backend의 설계 지시에 따라 작업한다.

## 담당 범위
```
backend/src/main/java/com/kanva/
├── domain/task/            # Task, TaskStatus, TaskRepository
├── domain/taskseries/      # TaskSeries, CompletionPolicy, ExcludedDate
├── controller/task/        # TaskController
├── controller/taskseries/  # TaskSeriesController
├── service/*/Task*         # Task 서비스
├── service/*/TaskSeries*   # TaskSeries 서비스
├── dto/task/               # TaskRequest, TaskResponse, StatusUpdate, PositionUpdate
└── dto/taskseries/         # TaskSeriesRequest, TaskSeriesResponse
```

## 규칙
- `frontend/` 절대 수정 금지
- 담당 범위 외 백엔드 코드 수정 시 TL-Backend 승인 필요
- TaskSeries 로직 변경 시 CompletionPolicy 영향 범위 확인
