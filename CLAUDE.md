# Kanva 프로젝트 개발 가이드

## ⚠️ Git 커밋 규칙

- 커밋 메시지에 `Co-Authored-By` 라인 **금지**
- 커밋 메시지는 한글 또는 영어로 간결하게 작성

---

## 📋 프로젝트 개요

**Kanva** - 실시간 동기화 개인 생산성 관리 앱

- 서비스/솔루션 회사 이직용 포트폴리오
- Spring Boot + React 풀스택 기술 스택
- WebSocket 실시간 멀티 디바이스 동기화
- Slack 연동 자동 알림
- **실제 매일 사용하는 실전 앱**
- **TDD(Test-Driven Development) 방식으로 개발**

### 💡 차별화 포인트
```
"제가 매일 실제로 사용하는 앱입니다"
→ Slack 알림 스크린샷
→ 멀티 디바이스 실시간 동기화 영상
→ 3주간 매일 사용한 데이터
```

---

## 📊 구현 현황

### 엔티티
| 엔티티 | Entity | Repository | Service | Controller | Test | 상태 |
|--------|--------|------------|---------|------------|------|------|
| User | ✅ | ✅ | ❌ | ❌ | ✅ | 기본 완료 |
| DailyNote | ✅ | ✅ | ✅ | ✅ | ✅ | **완료** |
| Task | ✅ | ✅ | ✅ | ✅ | ✅ | **완료** |
| Attachment | ❌ | ❌ | ❌ | ❌ | ❌ | 미구현 |
| SlackIntegration | ❌ | ❌ | ❌ | ❌ | ❌ | 미구현 |

### 구현된 API 엔드포인트

**DailyNote API**
| Method | URL | 설명 |
|--------|-----|------|
| GET | `/api/daily-notes?date=` | 특정 날짜 노트 조회 (없으면 생성) |
| PUT | `/api/daily-notes?date=` | 노트 수정 |
| DELETE | `/api/daily-notes?date=` | 노트 삭제 |
| GET | `/api/daily-notes/calendar?month=` | 월별 노트 목록 |

**Task API**
| Method | URL | 설명 |
|--------|-----|------|
| GET | `/api/tasks?date=` | 특정 날짜 Task 목록 |
| GET | `/api/tasks/{taskId}` | Task 단건 조회 |
| POST | `/api/tasks?date=` | Task 생성 |
| PUT | `/api/tasks/{taskId}` | Task 수정 |
| PATCH | `/api/tasks/{taskId}/status` | 상태 변경 |
| DELETE | `/api/tasks/{taskId}` | Task 삭제 |
| PUT | `/api/tasks/positions?date=` | 순서 변경 (드래그앤드롭) |
| GET | `/api/tasks/overdue` | 마감 지난 Task 목록 |

---

## 🛠 기술 스택

### Backend
- Java 21
- Spring Boot 3.5.9
- PostgreSQL
- Redis (캐싱)
- WebSocket (STOMP) - 실시간 동기화
- Slack API - 알림 연동
- Spring Scheduler - 정기 알림
- AWS S3 - 파일 업로드

### Frontend
- React 18
- TypeScript
- Material-UI
- react-markdown - 마크다운 렌더링
- toast-ui/editor - 마크다운 에디터
- react-calendar - 캘린더
- WebSocket Client (STOMP.js)

### Infra
- Docker
- AWS (EC2, RDS, S3, ElastiCache)
- GitHub Actions (CI/CD)

### 테스트
- JUnit 5
- Mockito
- AssertJ
- Spring Boot Test
- Testcontainers (통합 테스트)

---

## 🏗 아키텍처

### 레이어드 아키텍처 (Layered Architecture)

```
Controller (Presentation)  ← REST API, WebSocket
     ↓
Service (Business Logic)   ← 비즈니스 로직
     ↓
Repository (Data Access)   ← JPA, 데이터 접근
     ↓
Entity (Domain)            ← 도메인 모델
```

**적용 이유**
- 실무에서 가장 널리 사용되는 검증된 구조
- 계층별 책임 분리로 유지보수성 향상
- 테스트 용이성

---

## 📦 패키지 구조

```
src/main/java/com/kanva/
├── KanvaApplication.java
├── domain/                    # Entity, Repository
│   ├── common/
│   │   └── BaseEntity.java           ✅ 구현완료
│   ├── user/
│   │   ├── Role.java                 ✅ 구현완료
│   │   ├── User.java                 ✅ 구현완료
│   │   └── UserRepository.java       ✅ 구현완료
│   ├── dailynote/
│   │   ├── DailyNote.java            ✅ 구현완료
│   │   └── DailyNoteRepository.java  ✅ 구현완료
│   ├── task/
│   │   ├── TaskStatus.java           ✅ 구현완료
│   │   ├── Task.java                 ✅ 구현완료
│   │   └── TaskRepository.java       ✅ 구현완료
│   ├── attachment/                   ❌ 미구현
│   └── slack/                        ❌ 미구현
├── service/                   # 비즈니스 로직
│   ├── DailyNoteService.java         ✅ 구현완료
│   ├── TaskService.java              ✅ 구현완료
│   ├── impl/
│   │   ├── DailyNoteServiceImpl.java ✅ 구현완료
│   │   └── TaskServiceImpl.java      ✅ 구현완료
│   ├── UserService.java              ❌ 미구현 (인증 구현 시)
│   ├── AttachmentService.java        ❌ 미구현
│   └── SlackService.java             ❌ 미구현
├── presentation/rest/         # REST API (controller → presentation/rest로 변경)
│   ├── dailynote/
│   │   └── DailyNoteController.java  ✅ 구현완료
│   └── task/
│       └── TaskController.java       ✅ 구현완료
├── dto/                       # Request/Response DTO
│   ├── dailynote/
│   │   ├── DailyNoteRequest.java     ✅ 구현완료
│   │   └── DailyNoteResponse.java    ✅ 구현완료
│   └── task/
│       ├── TaskRequest.java          ✅ 구현완료
│       ├── TaskResponse.java         ✅ 구현완료
│       ├── TaskStatusUpdateRequest.java    ✅ 구현완료
│       └── TaskPositionUpdateRequest.java  ✅ 구현완료
├── common/                    # 공통 모듈
│   └── response/
│       └── ApiResponse.java          ✅ 구현완료
├── config/                    # 설정
│   ├── JpaConfig.java                ✅ 구현완료
│   └── SecurityConfig.java           ✅ 구현완료
├── security/                  # JWT, Security (추후 구현)
├── websocket/                 # 실시간 통신 (추후 구현)
├── scheduler/                 # 정기 작업 (추후 구현)
└── exception/                 # 예외 처리
    ├── GlobalExceptionHandler.java   ✅ 구현완료
    ├── UserNotFoundException.java    ✅ 구현완료
    ├── DailyNoteNotFoundException.java ✅ 구현완료
    └── TaskNotFoundException.java    ✅ 구현완료

src/test/java/com/kanva/      # 테스트 코드 (동일 구조)
├── domain/
│   ├── user/
│   │   ├── UserTest.java             ✅ 구현완료
│   │   └── UserRepositoryTest.java   ✅ 구현완료
│   ├── dailynote/
│   │   ├── DailyNoteTest.java        ✅ 구현완료
│   │   └── DailyNoteRepositoryTest.java ✅ 구현완료
│   └── task/
│       ├── TaskTest.java             ✅ 구현완료
│       └── TaskRepositoryTest.java   ✅ 구현완료
├── service/
│   ├── DailyNoteServiceTest.java     ✅ 구현완료
│   └── TaskServiceTest.java          ✅ 구현완료
└── presentation/rest/
    ├── dailynote/
    │   └── DailyNoteControllerTest.java ✅ 구현완료
    └── task/
        └── TaskControllerTest.java   ✅ 구현완료
```

---

## 📊 엔티티 설계

### ERD

```
User (1) ──────< DailyNote (N)
  │                │
  │                ├──< Task (N)
  │                │
  │                └──< Attachment (N)
  │
  └──────< SlackIntegration (1)
```

### 1. BaseEntity

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
```

### 2. User (사용자)

```java
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    
    @Column(nullable = false)
    private String password;  // BCrypt
    
    @Column(nullable = false, length = 50)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;  // USER, ADMIN
    
    @OneToMany(mappedBy = "user")
    private List<DailyNote> dailyNotes = new ArrayList<>();
    
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private SlackIntegration slackIntegration;
}

public enum Role {
    USER("일반 사용자"),
    ADMIN("관리자");
    
    private final String description;
}
```

### 3. DailyNote (일일 노트)

```java
@Entity
@Table(name = "daily_notes", 
    indexes = @Index(name = "idx_user_date", columnList = "user_id, date"),
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "date"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DailyNote extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private LocalDate date;  // 2025-01-18
    
    @Column(columnDefinition = "TEXT")
    private String content;  // 마크다운 내용
    
    @OneToMany(mappedBy = "dailyNote", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> tasks = new ArrayList<>();
    
    @OneToMany(mappedBy = "dailyNote", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attachment> attachments = new ArrayList<>();
    
    // 비즈니스 메서드
    public void updateContent(String content) {
        this.content = content;
    }
    
    public void addTask(Task task) {
        this.tasks.add(task);
        task.assignToDailyNote(this);
    }
}
```

### 4. Task (할 일)

```java
@Entity
@Table(name = "tasks",
    indexes = @Index(name = "idx_daily_note_position", columnList = "daily_note_id, position")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Task extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_note_id", nullable = false)
    private DailyNote dailyNote;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;  // 상세 설명 (마크다운)

    private LocalDate dueDate;  // 마감일 (nullable)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TaskStatus status = TaskStatus.PENDING;

    @Column(nullable = false)
    private Integer position;  // 드래그앤드롭 순서

    // 비즈니스 메서드
    public void updateStatus(TaskStatus status) {
        this.status = status;
    }

    public void complete() {
        this.status = TaskStatus.COMPLETED;
    }

    public void start() {
        this.status = TaskStatus.IN_PROGRESS;
    }

    public void updatePosition(Integer position) {
        this.position = position;
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateDescription(String description) {
        this.description = description;
    }

    public void updateDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    void assignToDailyNote(DailyNote dailyNote) {
        this.dailyNote = dailyNote;
    }

    public boolean isCompleted() {
        return this.status == TaskStatus.COMPLETED;
    }
}

public enum TaskStatus {
    PENDING("대기"),
    IN_PROGRESS("진행 중"),
    COMPLETED("완료");

    private final String description;

    TaskStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
```

### 5. Attachment (첨부파일)

```java
@Entity
@Table(name = "attachments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Attachment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_note_id", nullable = false)
    private DailyNote dailyNote;
    
    @Column(nullable = false)
    private String fileName;  // "screenshot.png"
    
    @Column(nullable = false)
    private String fileUrl;   // S3 URL
    
    @Column(nullable = false)
    private String fileType;  // "image/png"
    
    @Column(nullable = false)
    private Long fileSize;    // bytes
}
```

### 6. SlackIntegration (Slack 연동 설정)

```java
@Entity
@Table(name = "slack_integrations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SlackIntegration extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @Column(nullable = false, length = 500)
    private String webhookUrl;  // Slack Webhook URL
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean morningNotification = true;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean completionNotification = false;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean eveningReport = true;
    
    @Column(nullable = false, length = 5)
    @Builder.Default
    private String morningTime = "09:00";
    
    @Column(nullable = false, length = 5)
    @Builder.Default
    private String eveningTime = "20:00";
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;
    
    // 비즈니스 메서드
    public void updateSettings(Boolean morning, Boolean completion, Boolean evening) {
        this.morningNotification = morning;
        this.completionNotification = completion;
        this.eveningReport = evening;
    }
    
    public void updateTimes(String morningTime, String eveningTime) {
        this.morningTime = morningTime;
        this.eveningTime = eveningTime;
    }
    
    public void disable() {
        this.enabled = false;
    }
    
    public void enable() {
        this.enabled = true;
    }
}
```

---

## 📨 API 설계

### DTO 구조 원칙
- **Request 통합**: Create/Update 분리하지 않고 하나로 사용 (null 체크로 처리)
- **Response 분리**: 상세/목록 Response 분리
- **공통 응답**: ApiResponse<T> 래퍼
- **페이징**: PageRequestDto/PageResultDto

---

## 🧪 TDD 개발 프로세스

### Red-Green-Refactor 사이클

```
1. RED    : 실패하는 테스트 작성
2. GREEN  : 테스트를 통과하는 최소한의 코드 작성
3. REFACTOR : 코드 개선 (테스트는 계속 통과)
```

### TDD 개발 순서

**1단계: 도메인 (Entity, Repository)**
```
테스트 작성 → 엔티티 구현 → Repository 구현
```

**2단계: 서비스 (Business Logic)**
```
Service 테스트 작성 → Service 구현
```

**3단계: 컨트롤러 (REST API)**
```
Controller 테스트 작성 → REST API 구현
```

**4단계: 통합 기능 (WebSocket, Slack, Scheduler)**
```
통합 테스트 → 구현 → E2E 테스트
```

---

## 🎯 핵심 구현 포인트

### 1. WebSocket 실시간 동기화

**시나리오**: PC에서 할 일 체크 → 모바일에서 실시간 반영

```java
// WebSocketConfig
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*")
            .withSockJS();
    }
}

// TaskService에서 실시간 전송
@Service
@RequiredArgsConstructor
public class TaskService {
    private final SimpMessagingTemplate messagingTemplate;
    
    @Transactional
    public void toggleTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new TaskNotFoundException(taskId));
        
        task.toggle();
        
        // WebSocket으로 실시간 브로드캐스트
        TaskUpdateMessage message = TaskUpdateMessage.of(task, "TOGGLE");
        messagingTemplate.convertAndSend(
            "/topic/user/" + task.getDailyNote().getUser().getId(),
            message
        );
    }
}
```

**WebSocket 메시지 DTO**
```java
public record TaskUpdateMessage(
    String type,        // CREATED, UPDATED, DELETED, STATUS_CHANGED
    Long taskId,
    Long dailyNoteId,
    LocalDate date,
    TaskResponse task,
    LocalDateTime timestamp
) {
    public static TaskUpdateMessage of(Task task, String type) {
        return new TaskUpdateMessage(
            type,
            task.getId(),
            task.getDailyNote().getId(),
            task.getDailyNote().getDate(),
            TaskResponse.from(task),
            LocalDateTime.now()
        );
    }
}
```

---

### 1-1. WebSocket vs REST Polling 비교

#### 동작 방식 차이

```
┌─────────────────────────────────────────────────────────────────┐
│                    REST Polling (기존 방식)                      │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Client          Server                                         │
│    │                │                                           │
│    │── GET /tasks ─→│  (1초마다 반복 요청)                       │
│    │←─ 200 OK ─────│                                           │
│    │                │                                           │
│    │── GET /tasks ─→│  ← 변경 없어도 계속 요청                   │
│    │←─ 200 OK ─────│                                           │
│    │                │                                           │
│    │── GET /tasks ─→│  ← 서버 부하 증가                         │
│    │←─ 200 OK ─────│                                           │
│                                                                 │
│  문제점: 불필요한 요청, 지연시간, 서버 부하                       │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                    WebSocket (실시간 방식)                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Client          Server                                         │
│    │                │                                           │
│    │══ 연결 수립 ══►│  (한 번만 연결)                            │
│    │                │                                           │
│    │    (대기)      │  ← 연결 유지, 요청 없음                    │
│    │                │                                           │
│    │◄── PUSH ──────│  ← 변경 시에만 서버가 전송                 │
│    │                │                                           │
│    │    (대기)      │                                           │
│    │                │                                           │
│    │◄── PUSH ──────│  ← 즉시 반영 (지연 없음)                   │
│                                                                 │
│  장점: 즉시 반영, 서버 부하 감소, 양방향 통신                     │
└─────────────────────────────────────────────────────────────────┘
```

#### 성능 비교표

| 항목 | REST Polling | WebSocket |
|------|-------------|-----------|
| 지연시간 | 0~1초 (폴링 간격) | **즉시** (~50ms) |
| 서버 부하 | 높음 (불필요한 요청) | **낮음** (이벤트 기반) |
| 네트워크 | 매 요청마다 헤더 전송 | **연결 후 헤더 없음** |
| 실시간성 | 폴링 간격에 의존 | **진정한 실시간** |
| 구현 복잡도 | 단순 | 중간 |
| 연결 유지 | 매번 새 연결 | 지속 연결 |

---

### 1-2. WebSocket 실시간 동기화 시각적 데모 방법

포트폴리오에서 WebSocket을 **효과적으로 보여주는 방법**:

#### 방법 1: 분할 화면 녹화 (가장 효과적)

```
┌────────────────────┬────────────────────┐
│                    │                    │
│    PC 브라우저     │   모바일/태블릿     │
│                    │                    │
│  ☐ Task 1         │  ☐ Task 1         │
│  ☑ Task 2  ←클릭  │  ☑ Task 2  ←즉시반영│
│  ☐ Task 3         │  ☐ Task 3         │
│                    │                    │
└────────────────────┴────────────────────┘
         ↑ 동시에 녹화하여 실시간 동기화 증명
```

**녹화 시나리오:**
1. PC와 모바일 화면을 나란히 배치
2. PC에서 Task 상태 변경 (PENDING → IN_PROGRESS)
3. 모바일에서 **즉시** 반영되는 것 확인
4. 반대로 모바일에서 변경 → PC 반영 확인

#### 방법 2: 브라우저 개발자 도구 활용

```javascript
// 브라우저 콘솔에서 WebSocket 메시지 로그 표시
// Network 탭 → WS 필터 → 메시지 확인

// 화면에 실시간 로그 표시 (개발/데모용)
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, () => {
    stompClient.subscribe('/topic/user/1', (message) => {
        console.log('🔔 WebSocket 수신:', JSON.parse(message.body));
        // UI에 토스트 메시지 표시
        showToast('실시간 업데이트 수신!');
    });
});
```

#### 방법 3: 실시간 알림 배지 + 애니메이션

```
┌─────────────────────────────────────┐
│  📋 오늘의 할 일                     │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ ☑ API 개발        [완료]    │←── 상태 변경 시
│  │                   ✨애니메이션│    하이라이트 효과
│  └─────────────────────────────┘   │
│                                     │
│  🔔 "다른 기기에서 업데이트됨"       │←── 토스트 알림
│                                     │
└─────────────────────────────────────┘
```

**React 구현 예시:**
```jsx
// 실시간 업데이트 시 하이라이트 효과
const TaskItem = ({ task, isJustUpdated }) => {
    return (
        <div className={`task-item ${isJustUpdated ? 'highlight-animation' : ''}`}>
            <StatusBadge status={task.status} />
            <span>{task.title}</span>
            {isJustUpdated && <span className="sync-badge">🔄 동기화됨</span>}
        </div>
    );
};

// CSS 애니메이션
.highlight-animation {
    animation: highlight 1s ease-out;
}

@keyframes highlight {
    0% { background-color: #fffde7; }
    100% { background-color: transparent; }
}
```

#### 방법 4: 연결 상태 표시기

```
┌─────────────────────────────────────┐
│  Kanva                    🟢 실시간 │ ← 연결 상태 표시
│                                     │
│  연결 상태: Connected (WebSocket)   │
│  마지막 동기화: 방금 전              │
└─────────────────────────────────────┘

// 연결 끊김 시
┌─────────────────────────────────────┐
│  Kanva                    🔴 오프라인│
│                                     │
│  ⚠️ 연결이 끊어졌습니다.            │
│  재연결 시도 중... (3초 후)          │
└─────────────────────────────────────┘
```

#### 포트폴리오 데모 영상 시나리오

```
[0:00-0:05] 인트로 - "실시간 멀티 디바이스 동기화"

[0:05-0:15] PC와 모바일 화면 분할 표시
            - 두 기기 모두 같은 Task 목록 보여줌

[0:15-0:25] PC에서 Task 상태 변경
            - "API 개발" PENDING → IN_PROGRESS
            - 모바일에서 즉시 반영 (0.05초 이내)
            - 하이라이트 애니메이션 표시

[0:25-0:35] 모바일에서 새 Task 추가
            - PC에서 즉시 반영
            - 브라우저 개발자 도구에서 WebSocket 메시지 표시

[0:35-0:45] 네트워크 탭 확인
            - "REST Polling이었다면 1초마다 요청"
            - "WebSocket은 변경 시에만 메시지"

[0:45-0:50] 아웃트로 - 기술 스택 표시
```

---

### 2. Slack API 연동

**아침 알림 (매일 9시)**
```java
@Service
@RequiredArgsConstructor
public class SlackService {
    private final RestTemplate restTemplate;
    
    public void sendMorningTasks(String webhookUrl, List<Task> tasks, LocalDate date) {
        SlackMessage message = SlackMessage.builder()
            .text("📅 " + formatDate(date))
            .attachments(List.of(
                SlackAttachment.builder()
                    .color("#36a64f")
                    .title("오늘 할 일 (" + tasks.size() + "개)")
                    .text(formatTasks(tasks))
                    .footer("Kanva Bot")
                    .build()
            ))
            .build();
        
        restTemplate.postForEntity(webhookUrl, message, String.class);
    }
    
    private String formatTasks(List<Task> tasks) {
        return tasks.stream()
            .map(task -> "☐ " + task.getTitle())
            .collect(Collectors.joining("\n"));
    }
    
    private String formatDate(LocalDate date) {
        // "2025년 1월 18일 토요일"
        return date.format(DateTimeFormatter.ofPattern("yyyy년 M월 d일 E요일", Locale.KOREAN));
    }
}
```

**저녁 리포트 (매일 8시)**
```java
public void sendEveningReport(String webhookUrl, DailyReport report, LocalDate date) {
    int total = report.totalTasks();
    int completed = report.completedTasks();
    int percentage = (int) ((double) completed / total * 100);
    
    SlackMessage message = SlackMessage.builder()
        .text("📊 오늘의 성과")
        .attachments(List.of(
            SlackAttachment.builder()
                .color("#2eb886")
                .title(formatDate(date))
                .fields(List.of(
                    SlackField.of("완료율", percentage + "% (" + completed + "/" + total + ")"),
                    SlackField.of("완료", formatCompletedTasks(report.completedTaskTitles())),
                    SlackField.of("미완료", formatIncompleteTasks(report.incompleteTaskTitles()))
                ))
                .footer("내일도 화이팅! 🔥")
                .build()
        ))
        .build();
    
    restTemplate.postForEntity(webhookUrl, message, String.class);
}
```

---

### 3. Spring Scheduler

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class SlackNotificationScheduler {
    private final UserRepository userRepository;
    private final DailyNoteService dailyNoteService;
    private final TaskService taskService;
    private final SlackService slackService;
    
    // 매일 아침 9시
    @Scheduled(cron = "0 0 9 * * *")
    public void sendMorningNotifications() {
        log.info("Sending morning notifications...");
        
        LocalDate today = LocalDate.now();
        List<User> users = userRepository.findAllWithSlackEnabled();
        
        for (User user : users) {
            try {
                SlackIntegration slack = user.getSlackIntegration();
                if (slack.getMorningNotification()) {
                    List<Task> tasks = taskService.getTodayTasks(user.getId(), today);
                    slackService.sendMorningTasks(slack.getWebhookUrl(), tasks, today);
                }
            } catch (Exception e) {
                log.error("Failed to send morning notification to user: {}", user.getId(), e);
            }
        }
    }
    
    // 매일 저녁 8시
    @Scheduled(cron = "0 0 20 * * *")
    public void sendEveningReports() {
        log.info("Sending evening reports...");
        
        LocalDate today = LocalDate.now();
        List<User> users = userRepository.findAllWithSlackEnabled();
        
        for (User user : users) {
            try {
                SlackIntegration slack = user.getSlackIntegration();
                if (slack.getEveningReport()) {
                    DailyReport report = taskService.getDailyReport(user.getId(), today);
                    slackService.sendEveningReport(slack.getWebhookUrl(), report, today);
                }
            } catch (Exception e) {
                log.error("Failed to send evening report to user: {}", user.getId(), e);
            }
        }
    }
}
```

**application.yml 설정**
```yaml
spring:
  task:
    scheduling:
      pool:
        size: 5
```

---

### 4. AWS S3 파일 업로드

```java
@Service
@RequiredArgsConstructor
public class AttachmentService {
    private final AmazonS3 s3Client;
    private final AttachmentRepository attachmentRepository;
    
    @Value("${aws.s3.bucket}")
    private String bucketName;
    
    @Transactional
    public AttachmentResponse uploadFile(Long dailyNoteId, MultipartFile file) {
        // 파일명 중복 방지
        String fileName = generateUniqueFileName(file.getOriginalFilename());
        
        // S3 업로드
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());
        
        try {
            s3Client.putObject(
                bucketName,
                fileName,
                file.getInputStream(),
                metadata
            );
        } catch (IOException e) {
            throw new FileUploadException("파일 업로드 실패", e);
        }
        
        // S3 URL 생성
        String fileUrl = s3Client.getUrl(bucketName, fileName).toString();
        
        // DB 저장
        DailyNote dailyNote = dailyNoteRepository.findById(dailyNoteId)
            .orElseThrow(() -> new DailyNoteNotFoundException(dailyNoteId));
        
        Attachment attachment = Attachment.builder()
            .dailyNote(dailyNote)
            .fileName(file.getOriginalFilename())
            .fileUrl(fileUrl)
            .fileType(file.getContentType())
            .fileSize(file.getSize())
            .build();
        
        attachmentRepository.save(attachment);
        
        return AttachmentResponse.from(attachment);
    }
    
    private String generateUniqueFileName(String originalFilename) {
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        return UUID.randomUUID().toString() + extension;
    }
}
```

**S3Config**
```java
@Configuration
public class S3Config {
    @Value("${aws.access-key}")
    private String accessKey;
    
    @Value("${aws.secret-key}")
    private String secretKey;
    
    @Value("${aws.region}")
    private String region;
    
    @Bean
    public AmazonS3 s3Client() {
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        return AmazonS3ClientBuilder.standard()
            .withCredentials(new AWSStaticCredentialsProvider(credentials))
            .withRegion(region)
            .build();
    }
}
```

---

### 5. N+1 쿼리 최적화

```java
// DailyNoteRepository
@Query("""
    SELECT DISTINCT d FROM DailyNote d
    LEFT JOIN FETCH d.tasks
    LEFT JOIN FETCH d.attachments
    WHERE d.id = :id
    """)
Optional<DailyNote> findByIdWithTasksAndAttachments(@Param("id") Long id);

@Query("""
    SELECT DISTINCT d FROM DailyNote d
    LEFT JOIN FETCH d.user
    LEFT JOIN FETCH d.tasks
    WHERE d.user.id = :userId
    AND d.date BETWEEN :startDate AND :endDate
    ORDER BY d.date DESC
    """)
List<DailyNote> findByUserIdAndDateRange(
    @Param("userId") Long userId,
    @Param("startDate") LocalDate startDate,
    @Param("endDate") LocalDate endDate
);
```

---

### 6. Redis 캐싱

```java
@Service
@RequiredArgsConstructor
public class DailyNoteService {
    
    @Cacheable(value = "dailyNotes", key = "#userId + ':' + #date")
    @Transactional(readOnly = true)
    public DailyNoteResponse getDailyNote(Long userId, LocalDate date) {
        // 조회 로직
    }
    
    @CacheEvict(value = "dailyNotes", key = "#userId + ':' + #date")
    @Transactional
    public void updateDailyNote(Long userId, LocalDate date, DailyNoteRequest request) {
        // 업데이트 로직
    }
}
```

---

## 📈 테스트 커버리지 목표

- **전체 커버리지**: 80% 이상
- **도메인 레이어**: 90% 이상
- **서비스 레이어**: 85% 이상
- **컨트롤러 레이어**: 75% 이상

### 커버리지 측정
```bash
# Gradle
./gradlew test jacocoTestReport

# 리포트 확인
open build/reports/jacoco/test/html/index.html
```

---

## 📝 포트폴리오 작성 예시

```
[Kanva] 실시간 개인 생산성 관리 앱

"매일 사용하며 Slack으로 자동 알림받는 실전 앱"

기술 스택: Java 21, Spring Boot 3.5, React, PostgreSQL, Redis, WebSocket, Slack API

핵심 기능:
• 날짜 기반 ToDo + 마크다운 노트
• WebSocket 실시간 동기화 (멀티 디바이스)
• Slack 연동 (아침 할 일 알림, 저녁 리포트)
• Spring Scheduler 자동화
• AWS S3 파일 업로드
• 월간 활동 통계 + 히트맵

주요 구현:
• TDD 방식 개발 (테스트 커버리지 85%)
• N+1 쿼리 제거 (Fetch Join), 평균 응답시간 80ms 달성
• Redis 캐싱으로 조회 성능 개선 (히트율 75%)
• WebSocket으로 실시간 멀티 디바이스 동기화
• Slack API + Cron Scheduler 자동 알림
• AWS S3 파일 업로드/다운로드

실사용 증명:
→ Slack에 매일 알림 받는 스크린샷
→ 여러 디바이스에서 실시간 동기화 영상
→ 3주간 실제 사용 데이터

GitHub: github.com/your-username/kanva
Demo: kanva-demo.com
```

---

## ✅ TDD 개발 체크리스트

**도메인 레이어**
- [x] Entity 테스트 작성 → 구현 (User, DailyNote, Task)
- [x] Repository 테스트 작성 → 구현 (UserRepository, DailyNoteRepository, TaskRepository)
- [x] 비즈니스 로직 테스트 → 구현 (상태 변경, 위치 변경 등)

**서비스 레이어**
- [x] Service 테스트 → 구현 (DailyNoteService, TaskService)
- [x] DTO 변환 테스트 (TaskResponse.from, DailyNoteResponse.from)

**컨트롤러 레이어**
- [x] Controller 단위 테스트 (@WebMvcTest) - DailyNoteController, TaskController
- [ ] API 통합 테스트 (@SpringBootTest)
- [x] Validation 테스트 (title 필수값 등)

**통합 기능**
- [ ] WebSocket 테스트
- [ ] Slack API 테스트
- [ ] Scheduler 테스트
- [ ] S3 업로드 테스트

**통합 테스트**
- [ ] Testcontainers 설정
- [ ] E2E 시나리오 테스트

**추가 구현 필요**
- [ ] User 인증 (JWT, Spring Security)
- [ ] Attachment 엔티티 및 S3 연동
- [ ] SlackIntegration 엔티티 및 Slack API 연동
- [ ] WebSocket 실시간 동기화
- [ ] Spring Scheduler 알림

---

## 🔍 TDD 개발 팁

1. **작은 단위로 시작**: 한 번에 하나의 기능만 테스트
2. **실패하는 테스트 먼저**: Red 단계를 명확히
3. **최소한의 코드**: Green을 위한 최소 구현
4. **지속적인 리팩토링**: 테스트가 있으니 안전하게 개선
5. **테스트 가독성**: Given-When-Then 패턴 준수
6. **Mock 최소화**: 가능한 실제 객체 사용
7. **통합 테스트로 검증**: 마지막에 전체 흐름 확인

---

**개발 시작 시**: 테스트 먼저 작성 → 실패 확인 → 구현 → 통과 → 리팩토링
