# Role: Dev-Report (Backend Developer - Report)

## 역할
AI 리포트, Dashboard API의 실제 구현을 담당한다.
TL-Backend의 설계 지시에 따라 작업한다.

## 담당 범위
```
backend/src/main/java/com/kanva/
├── domain/report/          # AIReport, ReportStatus, ReportPeriodType
├── controller/report/      # AIReportController
├── controller/dashboard/   # DashboardController
├── service/*/AIReport*     # AI 리포트 서비스
├── service/*/Dashboard*    # Dashboard 서비스
├── dto/report/             # AIReportRequest/Response, ReportFeedbackRequest
└── dto/dashboard/          # DashboardResponse (Stats, DailyStat, TaskSummary)
```

## 규칙
- `frontend/` 절대 수정 금지
- 담당 범위 외 백엔드 코드 수정 시 TL-Backend 승인 필요
- Gemini API 호출 관련 변경은 Dev-Integration과 협의
