# Kanva - Personal Productivity Management App

## Project Overview

**Kanva**는 개인 생산성 관리 앱입니다.

- **목적**: 서비스/솔루션 회사 이직용 포트폴리오
- **특징**: 매일 실제 사용하는 실전 앱
- **개발 방식**: TDD (Test-Driven Development)

---

## Team Structure (15명)

```
PM (1) — 스프린트 관리, 태스크 할당, 진행 추적
│
├── TL-Backend (1) — 백엔드 기술 설계/판단/리뷰
│   ├── Dev-Auth        — 인증/인가, OAuth, Security
│   ├── Dev-Task        — Task, TaskSeries
│   ├── Dev-Note        — DailyNote, 음성 STT
│   ├── Dev-Report      — AI 리포트, Dashboard API
│   └── Dev-Integration — Slack, Gemini, 외부 API 연동
│
├── TL-Frontend (1) — 프론트엔드 기술 설계/판단/리뷰
│   ├── Dev-Workspace   — DailyNoteEditor, TaskList 컴포넌트
│   ├── Dev-Dashboard   — Dashboard, Calendar 컴포넌트
│   └── Dev-Core        — AuthContext, API 래퍼, 공통 컴포넌트
│
├── TL-DevOps (1) — 인프라 설계/배포 전략
│   └── DevOps-Engineer — Docker, CI/CD, AWS 운영
│
└── TL-QA (1) — 테스트 전략/품질 관리
    └── QA-Engineer     — 테스트 작성/실행
```

### 역할 분리 원칙
- **TL**: 기술 설계, 코드 리뷰, 판단 → 직접 구현하지 않음
- **Dev/Engineer**: TL 지시에 따라 실제 구현
- 각 역할의 상세 정의: `.claude/roles/` 참조
- 태스크 관리: `.claude/tasks/` 참조

### 역할별 실행 방법
```bash
# 터미널별로 역할을 지정하여 실행 (기본 기능 유지 + 역할 추가)
# PM
claude --append-system-prompt-file .claude/roles/pm.md

# Backend (TL + Dev 5명)
claude --append-system-prompt-file .claude/roles/tl-backend.md
claude --append-system-prompt-file .claude/roles/dev-auth.md
claude --append-system-prompt-file .claude/roles/dev-task.md
claude --append-system-prompt-file .claude/roles/dev-note.md
claude --append-system-prompt-file .claude/roles/dev-report.md
claude --append-system-prompt-file .claude/roles/dev-integration.md

# Frontend (TL + Dev 3명)
claude --append-system-prompt-file .claude/roles/tl-frontend.md
claude --append-system-prompt-file .claude/roles/dev-workspace.md
claude --append-system-prompt-file .claude/roles/dev-dashboard.md
claude --append-system-prompt-file .claude/roles/dev-core.md

# DevOps (TL + Engineer 1명)
claude --append-system-prompt-file .claude/roles/tl-devops.md
claude --append-system-prompt-file .claude/roles/devops-engineer.md

# QA (TL + Engineer 1명)
claude --append-system-prompt-file .claude/roles/tl-qa.md
claude --append-system-prompt-file .claude/roles/qa-engineer.md
```

### 하네스 구조
- **CLAUDE.md** → 모든 세션에 공통 로드 (프로젝트 전체 맥락)
- **`.claude/roles/`** → 세션별 선택 로드 (역할 경계)
- **`.claude/tasks/`** → 역할 간 커뮤니케이션 (태스크 파일로 지시)

---

## Tech Stack

### Backend
- Java 21
- Spring Boot 3.5
- Spring Security + JWT (JJWT)
- OAuth 2.0 (GitHub, Slack)
- JPA/Hibernate
- PostgreSQL
- Slack API Client
- Lombok

### Frontend
- React 19
- TypeScript
- Vite
- Fetch API (Axios 미사용)
- React Context (상태 관리)
- react-markdown (노트 마크다운 렌더링)

### Infrastructure (예정)
- Docker
- AWS (EC2, RDS, S3)
- Redis (캐싱)
- WebSocket (STOMP)

---

## Project Structure

```
kanva/
├── backend/src/main/java/com/kanva/
│   ├── auth/oauth/provider/       # OAuth2UserInfo, GithubUserInfo, SlackUserInfo
│   ├── common/
│   │   ├── code/                  # ErrorCode, SuccessCode
│   │   └── response/              # ApiResponse
│   ├── config/                    # SecurityConfig, ClockConfig, JpaConfig, OAuthConfig
│   ├── controller/
│   │   ├── auth/                  # AuthController
│   │   ├── dailynote/             # DailyNoteController
│   │   ├── dashboard/             # DashboardController
│   │   ├── task/                  # TaskController
│   │   └── taskseries/            # TaskSeriesController
│   ├── domain/
│   │   ├── user/                  # User, Role, UserOAuthConnection, OAuthProvider
│   │   ├── dailynote/             # DailyNote
│   │   ├── task/                  # Task, TaskStatus
│   │   ├── taskseries/            # TaskSeries, CompletionPolicy, TaskSeriesExcludedDate
│   │   ├── slack/                 # SlackConnection
│   │   └── notification/          # NotificationLog, NotificationSlot, NotificationResult
│   ├── dto/
│   │   ├── auth/                  # OAuthCallbackRequest, OAuthLoginUrlResponse
│   │   ├── user/                  # LoginRequest/Response, SignUpRequest, UserResponse
│   │   ├── task/                  # TaskRequest, TaskResponse, StatusUpdate, PositionUpdate
│   │   ├── taskseries/            # TaskSeriesRequest, TaskSeriesResponse
│   │   ├── dailynote/             # Request, Response, DetailResponse, SummaryResponse
│   │   ├── dashboard/             # DashboardResponse (Stats, DailyStat, TaskSummary)
│   │   └── notification/          # SlackTarget, SlackSendResult
│   ├── exception/                 # GlobalExceptionHandler, 커스텀 예외들
│   ├── scheduler/                 # NotificationScheduler
│   ├── security/
│   │   ├── jwt/                   # JwtTokenProvider, JwtAuthenticationFilter, JwtToken
│   │   ├── UserPrincipal.java
│   │   ├── CustomUserDetailsService.java
│   │   └── CustomUserDetails.java
│   └── service/
│       ├── impl/                  # 서비스 구현체들
│       ├── notification/          # NotificationService, SlackDmSenderService
│       └── interfaces             # TaskService, UserService, OAuthService, etc.
│
└── frontend/src/
    ├── App.tsx                    # 페이지 전환 (수동, React Router 미사용)
    ├── main.tsx                   # React 엔트리포인트
    ├── index.css                  # 글로벌 스타일
    ├── components/
    │   ├── header/                # DateBadge, CalendarModal
    │   ├── note/                  # DailyNoteEditor
    │   ├── tasks/                 # TaskList, TaskItem, AddTaskRow, TaskDetailModal, SeriesDeleteModal
    │   └── dashboard/             # MonthSelector, TaskStats, ProductivityChart, DailyNotesList
    ├── contexts/                  # AuthContext (OAuth 상태 관리)
    ├── pages/                     # LoginPage, OAuthCallbackPage, DailyWorkspacePage, DashboardPage
    ├── services/                  # api.ts (authApi, taskApi, dailyNoteApi, dashboardApi, taskSeriesApi)
    └── types/                     # api.ts (TypeScript 타입 정의)
```

---

## Domain Entities

### User
- **Fields**: `id`, `email` (unique, max 100), `password` (nullable, BCrypt), `name` (max 50), `role` (USER/ADMIN), `picture` (max 500)
- **Relationships**: 1:N DailyNote, 1:N UserOAuthConnection
- **Methods**: `updateName()`, `updatePassword()`, `updatePicture()`

### UserOAuthConnection
- **Fields**: `id`, `user` (FK), `provider` (GITHUB/SLACK), `providerId` (max 100), `picture`
- **Constraints**: Unique (provider, providerId), Unique (user_id, provider)
- **용도**: 다중 OAuth Provider 연결 지원

### DailyNote
- **Fields**: `id`, `user` (FK), `date`, `content` (TEXT), `tasks` (1:N, cascade ALL, orphanRemoval)
- **Constraints**: Unique (user_id, date), Index: idx_user_date
- **Methods**: `updateContent()`, `addTask()`, `removeTask()`, `getCompletedTaskCount()`, `getTotalTaskCount()`

### Task
- **Fields**: `id`, `dailyNote` (FK, NOT NULL), `series` (FK, nullable), `taskDate`, `title` (max 200), `description` (TEXT), `dueDate`, `status` (PENDING/IN_PROGRESS/COMPLETED), `position`
- **Constraints**: Unique (series_id, task_date), Index: idx_daily_note_position
- **Methods**: `toggle()`, `complete()`, `start()`, `pending()`, `isOverdue()`, `isSeriesTask()`
- **로직**: 미래 날짜 Task 상태 변경 불가

### TaskSeries
- **Fields**: `id`, `user` (FK), `title`, `description`, `startDate`, `endDate`, `completionPolicy`, `stopDate`, `stopOnComplete`
- **CompletionPolicy**:
  - `PER_OCCURRENCE`: 인스턴스별 완료, 시리즈 계속 (습관 트래킹)
  - `COMPLETE_STOPS_SERIES`: 완료 시 stopDate 설정, 미래 인스턴스 삭제 (프로젝트)
- **Methods**: `canGenerateFor(date)`, `stop(taskDate)`, `forceStop(date)`, `isStopped()`, `isActive()`

### TaskSeriesExcludedDate
- **Composite PK**: `(taskSeriesId, date)` via @EmbeddedId
- **Pattern**: `Persistable<TaskSeriesExcludedDateId>` 구현 (persist/merge 구분)

### SlackConnection
- **Fields**: `id`, `user` (OneToOne FK), `slackUserId`, `teamId`, `teamName`, `botToken` (max 500), `notificationsEnabled`
- **Methods**: `updateBotToken()`, `enableNotifications()`, `disableNotifications()`

### NotificationLog
- **Fields**: `id`, `user` (FK), `slot` (MORNING/EVENING), `notificationDate`, `slackUserId`, `teamId`, `result` (SUCCESS/FAIL), `errorMessage`, `sentAt`, `retryCount`
- **Factory**: `success()`, `fail()`

---

## API Endpoints

### Auth
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/signup` | 회원가입 |
| POST | `/api/auth/login` | 로그인 → JWT 발급 |
| GET | `/api/auth/me` | 현재 사용자 정보 |
| GET | `/api/auth/oauth/{provider}/login-url` | OAuth 로그인 URL 조회 |
| POST | `/api/auth/oauth/{provider}/callback` | OAuth 콜백 처리 |

### Task
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/tasks?date=` | 날짜별 Task 조회 (온디맨드 시리즈 생성 포함) |
| GET | `/api/tasks/{id}` | 단건 조회 |
| POST | `/api/tasks?date=` | Task 생성 (repeatDaily=true 시 시리즈 자동 생성) |
| PUT | `/api/tasks/{id}` | Task 수정 |
| PATCH | `/api/tasks/{id}/status` | 상태 변경 |
| PATCH | `/api/tasks/{id}/toggle` | COMPLETED ↔ PENDING 토글 |
| DELETE | `/api/tasks/{id}` | 삭제 (시리즈 Task면 cleanupIfEligible 트리거) |
| PUT | `/api/tasks/positions?date=` | 드래그앤드롭 순서 변경 |
| GET | `/api/tasks/overdue` | 지연 Task 조회 |

### DailyNote
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/daily-notes/{date}` | 노트 조회 (없으면 자동 생성) |
| PUT | `/api/daily-notes/{date}` | 노트 수정 |
| DELETE | `/api/daily-notes/{date}` | 노트 삭제 |
| GET | `/api/daily-notes/calendar?month=` | 월별 요약 (날짜별 hasContent) |

### TaskSeries
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/task-series` | 시리즈 생성 |
| GET | `/api/task-series` | 전체 시리즈 조회 |
| GET | `/api/task-series/active` | 활성 시리즈만 조회 |
| POST | `/api/task-series/generate` | 오늘 생성 수동 트리거 (디버그용) |
| POST | `/api/task-series/{id}/exclude` | 특정 날짜 제외 (SKIP) |
| POST | `/api/task-series/{id}/stop` | 시리즈 중단 (STOP) |

### Dashboard
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/dashboard?month=` | 월별 통계 (단일 쿼리 최적화) |

---

## Key Features

### OAuth 인증 (GitHub, Slack)
- **Flow**:
  1. `GET /oauth/{provider}/login-url` → OAuth URL + state 반환
  2. Frontend에서 state를 sessionStorage에 저장 후 OAuth URL로 리다이렉트
  3. OAuth Provider가 `/login/oauth2/code/{provider}?code=&state=` 로 콜백
  4. `POST /oauth/{provider}/callback` → JWT 토큰 발급
- **다중 Provider 연결**: UserOAuthConnection으로 한 User가 여러 OAuth Provider 연결 가능
- **Slack 로그인 시**: SlackConnection 테이블에 botToken, teamId 저장 → 알림 발송용

### JWT 인증
- **Algorithm**: HMAC SHA-256
- **Claims**: sub(email), auth(authorities), userId, iat, exp
- **Filter**: Authorization: Bearer {token}
- **Token 저장**: localStorage (kanva_access_token, kanva_refresh_token)

### TaskSeries 생성 흐름
1. **직접 생성**: `POST /api/task-series` → 오늘이 범위 내면 즉시 인스턴스 생성
2. **Task에서 변환**: Task 생성/수정 시 `repeatDaily=true, endDate` 설정 → 자동 시리즈 생성
3. **온디맨드 생성**: `GET /api/tasks?date=` 호출 시 해당 날짜 시리즈 인스턴스 자동 생성

### TaskSeries Exclude/Stop
- **Exclude (SKIP)**: 특정 날짜만 제외, excluded_date 기록, 해당 날짜 Task 삭제
- **Stop**: forceStop(date) → stopDate = date - 1, 해당일 + 미래 Task 전부 삭제
- **Auto Cleanup**: 종료 의사 확정 + 남은 Task 0개 → 시리즈 + excluded_dates 자동 삭제

### Dashboard (단일 API 최적화)
- 월 범위 Task 한 번에 조회 → 메모리에서 그룹핑
- stats: completed, inProgress, pending, overdue
- dailyStats: 일별 totalCount, completedCount
- overdueTasks / dueSoonTasks: 최대 10개씩

---

## Schedulers

### NotificationScheduler
- **Morning**: `0 0 8 * * *` (매일 08:00 KST) - 오늘의 할 일 Slack DM
- **Evening**: `0 0 17 * * *` (매일 17:00 KST) - 미완료 Task 리마인더

---

## Frontend Components

### Pages
- **LoginPage**: GitHub/Slack OAuth 버튼
- **OAuthCallbackPage**: OAuth 콜백 처리, state 검증
- **DailyWorkspacePage**: 2컬럼 레이아웃 (DailyNoteEditor | TaskList)
- **DashboardPage**: 3컬럼 그리드 (MonthSelector | TaskStats | ProductivityChart)

### Key Components
- **DateBadge**: 7일 슬라이더 (주 단위 날짜 선택)
- **DailyNoteEditor**: Edit/Preview 모드, 마크다운 지원, dirty 추적
- **TaskList**: Task 목록 + SeriesDeleteModal + TaskDetailModal
- **TaskItem**: 상태 뱃지, 완료 시 strikethrough
- **TaskDetailModal**: Task 상세 편집, repeatDaily 설정
- **SeriesDeleteModal**: 시리즈 Task 삭제 시 Skip/Stop 선택
- **ProductivityChart**: 7일 슬라이딩 윈도우 바 차트

### Styling
- Global CSS (index.css) + Inline React.CSSProperties 혼합
- 반응형: 768px, 600px breakpoints

---

## Development Rules

### Git
- 한글 또는 영어로 간결하게 작성

### Code Style
- Backend: Java 표준 코딩 컨벤션, Lombok 활용
- Frontend: ESLint + TypeScript strict
- 한글 주석 사용 가능

### Architecture
- **Layered**: Controller → Service → Repository → Domain
- **DTO 변환**: Response 클래스의 `from()` 정적 메서드
- **예외 처리**: GlobalExceptionHandler에서 일괄 처리
- **API 응답**: `ApiResponse<T>` 래퍼 (success, data, code, message, errors)

### Key Patterns
- `@EmbeddedId` + `Persistable`: 복합키 엔티티의 persist/merge 구분
- Clock Bean: 테스트 가능한 시간 처리 (Asia/Seoul)
- On-demand 생성: 조회 시점에 필요한 데이터 자동 생성

---

## Local Development

### Backend
```bash
cd backend
./gradlew bootRun --args='--spring.profiles.active=local'
# http://localhost:8080
```

### Frontend
```bash
cd frontend
npm install
npm run dev
# http://localhost:5173
```

### 환경 설정
`backend/src/main/resources/application-local.properties`:
```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/kanva
spring.datasource.username=postgres
spring.datasource.password=1234

# JWT
jwt.secret=your-secret-key

# OAuth
oauth.github.clientId=xxx
oauth.github.clientSecret=xxx
oauth.github.redirectUri=http://localhost:5173/login/oauth2/code/github

oauth.slack.clientId=xxx
oauth.slack.clientSecret=xxx
oauth.slack.redirectUri=http://localhost:5173/login/oauth2/code/slack
```

---

## Error Codes

| Code | Status | Message |
|------|--------|---------|
| INVALID_INPUT | 400 | 입력값이 올바르지 않습니다 |
| MISSING_PARAMETER | 400 | 필수 파라미터가 누락되었습니다 |
| TASK_STATUS_CHANGE_NOT_ALLOWED | 400 | 미래 날짜의 Task는 상태를 변경할 수 없습니다 |
| UNAUTHORIZED | 401 | 인증이 필요합니다 |
| INVALID_CREDENTIALS | 401 | 이메일 또는 비밀번호가 일치하지 않습니다 |
| OAUTH_FAILED | 401 | OAuth 인증에 실패했습니다 |
| USER_NOT_FOUND | 404 | 사용자를 찾을 수 없습니다 |
| DAILY_NOTE_NOT_FOUND | 404 | 노트를 찾을 수 없습니다 |
| TASK_NOT_FOUND | 404 | 할 일을 찾을 수 없습니다 |
| DUPLICATE_EMAIL | 409 | 이미 사용 중인 이메일입니다 |
| INTERNAL_ERROR | 500 | 서버 내부 오류가 발생했습니다 |

---

## Implementation Status

### Completed
- [x] User Entity + JWT 인증
- [x] OAuth 2.0 (GitHub, Slack)
- [x] 다중 OAuth Provider 연결 (UserOAuthConnection)
- [x] DailyNote CRUD + 자동 생성
- [x] Task CRUD + 상태 관리
- [x] TaskSeries 반복 Task (온디맨드 + 스케줄러)
- [x] CompletionPolicy (PER_OCCURRENCE / COMPLETE_STOPS_SERIES)
- [x] TaskSeries Exclude/Stop + Auto Cleanup
- [x] Dashboard 월별 통계 (단일 쿼리 최적화)
- [x] Slack 알림 시스템 (SlackConnection + NotificationLog)
- [x] Frontend OAuth 로그인/콜백 페이지
- [x] Frontend DailyWorkspace + Dashboard 페이지

### TODO
- [ ] WebSocket 실시간 동기화
- [ ] 파일 첨부 (S3)
- [ ] Redis 캐싱
- [ ] 배포 환경 구성 (Docker, AWS)
