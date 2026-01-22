# Chronicle 프로젝트 일자별 개발 계획

**개발 기간**: 3주 (21일)  
**일일 작업 시간**: 6-8시간  
**개발 방식**: TDD (Test-Driven Development)  
**시작일**: 2025년 1월 18일 (토요일)

**프로젝트**: 실시간 동기화 개인 생산성 관리 앱 (ToDo + 마크다운 노트 + Slack 연동)

---

## 📅 Week 1: 도메인 & 기본 CRUD (7일)

### Day 1 (1/18, 토) - 프로젝트 세팅 & User/DailyNote Entity
**목표**: 개발 환경 구축 및 핵심 도메인 TDD 구현  
**예상 시간**: 6-7시간

#### 오전 (3시간)
**1. 프로젝트 생성 및 초기 설정 (1시간)**

**IntelliJ에서 Spring Initializr로 프로젝트 생성**
```
Name: chronicle
Group: com.chronicle
Artifact: chronicle
Package: com.chronicle
Java: 21
Spring Boot: 3.5.9

Dependencies:
- Spring Web
- Spring Data JPA
- PostgreSQL Driver
- Lombok
- Validation
- Spring Boot DevTools
```

**Docker Compose 실행**
```bash
docker-compose up -d
# PostgreSQL, Redis 실행 확인
```

**application.yml 기본 설정**
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/chronicle
    username: chronicle
    password: chronicle
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
    properties:
      hibernate:
        format_sql: true

# Test용
---
spring:
  config:
    activate:
      on-profile: test
  datasource:
    url: jdbc:h2:mem:testdb
  jpa:
    hibernate:
      ddl-auto: create-drop
```

**2. 공통 모듈 TDD (2시간)**

**✅ BaseEntity 테스트 먼저**
```bash
claude "BaseEntity 테스트 작성해줘.
테스트 케이스:
- createdAt이 자동 설정되는지
- updatedAt이 수정 시 자동 변경되는지

@DataJpaTest 사용
위치: src/test/java/com/chronicle/domain/BaseEntityTest.java"
```

**✅ BaseEntity 구현**
```bash
claude "BaseEntity 구현해줘.
- @MappedSuperclass
- @EntityListeners(AuditingEntityListener.class)
- createdAt, updatedAt (LocalDateTime)
- Lombok @Getter만

위치: src/main/java/com/chronicle/domain/BaseEntity.java"
```

**✅ JpaConfig**
```bash
claude "JpaConfig 만들어줘.
- @EnableJpaAuditing
위치: src/main/java/com/chronicle/config/JpaConfig.java"
```

**✅ 공통 응답 구조**
```bash
claude "공통 응답 구조 만들어줘.
1. SuccessCode enum (OK, CREATED, NO_CONTENT)
2. ErrorCode enum (BAD_REQUEST, UNAUTHORIZED, NOT_FOUND 등)
3. ApiResponse<T> record (success, code, message, data)
4. ErrorResponse record (timestamp, status, code, message, path)

위치: src/main/java/com/chronicle/common/"
```

#### 오후 (3-4시간)
**3. User Entity TDD (1.5시간)**

**RED: 테스트 작성**
```bash
claude "User 엔티티 테스트 작성해줘.
테스트 케이스:
- User 생성 성공 (Builder 패턴)
- email은 필수값 (null 시 예외)
- password는 필수값
- Role 기본값은 USER
- equals/hashCode는 id 기반

위치: src/test/java/com/chronicle/domain/user/UserTest.java"
```

**GREEN: 구현**
```bash
claude "User 엔티티 구현해줘.
@Entity, @Table(name = \"users\")
- id, email, password, name, role
- BaseEntity 상속
- Lombok @Getter, @Builder, @NoArgsConstructor, @AllArgsConstructor
- equals/hashCode 오버라이드 (id 기반)
- @OneToMany dailyNotes
- @OneToOne slackIntegration

위치: src/main/java/com/chronicle/domain/user/User.java"

claude "Role Enum 만들어줘.
- USER, ADMIN
- description 필드
위치: src/main/java/com/chronicle/domain/user/Role.java"
```

**✅ UserRepository TDD**
```bash
claude "UserRepository 테스트 작성해줘.
@DataJpaTest

테스트 케이스:
- save and findById
- findByEmail 성공
- findByEmail 실패 시 Optional.empty()
- existsByEmail true/false

위치: src/test/java/com/chronicle/domain/user/UserRepositoryTest.java"

claude "UserRepository 구현해줘.
- JpaRepository<User, Long>
- findByEmail
- existsByEmail

위치: src/main/java/com/chronicle/domain/user/UserRepository.java"
```

**4. DailyNote Entity TDD (1.5-2시간)**

**RED: 테스트**
```bash
claude "DailyNote 엔티티 테스트 작성해줘.
테스트 케이스:
- DailyNote 생성 성공
- user와 date는 필수값
- content는 선택값
- 같은 user의 같은 date는 중복 불가 (unique)
- updateContent 메서드
- addTask 메서드 (양방향 관계)

위치: src/test/java/com/chronicle/domain/dailynote/DailyNoteTest.java"
```

**GREEN: 구현**
```bash
claude "DailyNote 엔티티 구현해줘.
@Entity
@Table(name = \"daily_notes\",
    indexes = @Index(name = \"idx_user_date\", columnList = \"user_id, date\"),
    uniqueConstraints = @UniqueConstraint(columnNames = {\"user_id\", \"date\"})
)

필드:
- id, user (@ManyToOne LAZY), date (LocalDate), content (TEXT)
- tasks (@OneToMany, cascade ALL, orphanRemoval)
- attachments (@OneToMany, cascade ALL, orphanRemoval)

메서드:
- updateContent(String content)
- addTask(Task task)

위치: src/main/java/com/chronicle/domain/dailynote/DailyNote.java"
```

**✅ DailyNoteRepository TDD**
```bash
claude "DailyNoteRepository 테스트 작성해줘.
테스트 케이스:
- save and findById
- findByUserIdAndDate
- findByUserId 목록 조회
- 같은 user + date 중복 저장 시 예외

위치: src/test/java/com/chronicle/domain/dailynote/DailyNoteRepositoryTest.java"

claude "DailyNoteRepository 구현해줘.
- findByUserIdAndDate(Long userId, LocalDate date)
- findByUserId(Long userId)

위치: src/main/java/com/chronicle/domain/dailynote/DailyNoteRepository.java"
```

**✅ Day 1 완료 체크리스트**
- [ ] 프로젝트 생성 및 Docker 실행
- [ ] BaseEntity 구현 및 테스트 통과
- [ ] 공통 응답 구조 완성
- [ ] User Entity + Repository 테스트 통과
- [ ] DailyNote Entity + Repository 테스트 통과
- [ ] 모든 테스트 Green
- [ ] Git commit (feat: add User and DailyNote domain)

---

### Day 2 (1/19, 일) - Task & Attachment Entity
**목표**: 나머지 도메인 모델 완성  
**예상 시간**: 7-8시간

#### 오전 (4시간)
**1. Task Entity TDD (2.5시간)**

**RED: 테스트**
```bash
claude "Task 엔티티 테스트 작성해줘.
테스트 케이스:
- Task 생성 성공
- title 필수값
- completed 기본값 false
- position 필수값
- toggle() 메서드 (완료 토글)
- updatePosition() 메서드
- updateTitle() 메서드

위치: src/test/java/com/chronicle/domain/task/TaskTest.java"
```

**GREEN: 구현**
```bash
claude "Task 엔티티 구현해줘.
@Entity
@Table(name = \"tasks\",
    indexes = @Index(name = \"idx_daily_note_position\", columnList = \"daily_note_id, position\")
)

필드:
- id, dailyNote (@ManyToOne LAZY), title, completed (default false), position

메서드:
- toggle()
- updatePosition(Integer position)
- updateTitle(String title)
- assignToDailyNote(DailyNote dailyNote) - package-private

위치: src/main/java/com/chronicle/domain/task/Task.java"
```

**✅ TaskRepository TDD**
```bash
claude "TaskRepository 테스트 작성해줘.
테스트 케이스:
- save and findById
- findByDailyNoteId
- findByDailyNoteIdOrderByPosition (position 오름차순)

위치: src/test/java/com/chronicle/domain/task/TaskRepositoryTest.java"

claude "TaskRepository 구현해줘.
- findByDailyNoteId(Long dailyNoteId)
- findByDailyNoteIdOrderByPosition(Long dailyNoteId)

위치: src/main/java/com/chronicle/domain/task/TaskRepository.java"
```

**2. Attachment Entity TDD (1.5시간)**

**RED: 테스트**
```bash
claude "Attachment 엔티티 테스트 작성해줘.
테스트 케이스:
- Attachment 생성 성공
- fileName, fileUrl, fileType, fileSize 필수값
- dailyNote 연관관계

위치: src/test/java/com/chronicle/domain/attachment/AttachmentTest.java"
```

**GREEN: 구현**
```bash
claude "Attachment 엔티티 구현해줘.
@Entity
@Table(name = \"attachments\")

필드:
- id, dailyNote (@ManyToOne LAZY), fileName, fileUrl, fileType, fileSize

위치: src/main/java/com/chronicle/domain/attachment/Attachment.java"
```

**✅ AttachmentRepository**
```bash
claude "AttachmentRepository 테스트 및 구현해줘.
- findByDailyNoteId(Long dailyNoteId)

위치: src/test/.../attachment/, src/main/.../attachment/"
```

#### 오후 (3-4시간)
**3. SlackIntegration Entity TDD (2시간)**

**RED: 테스트**
```bash
claude "SlackIntegration 엔티티 테스트 작성해줘.
테스트 케이스:
- SlackIntegration 생성 성공
- webhookUrl 필수값
- 기본값 설정 (morningNotification=true, eveningReport=true, enabled=true)
- updateSettings 메서드
- updateTimes 메서드
- enable/disable 메서드

위치: src/test/java/com/chronicle/domain/slack/SlackIntegrationTest.java"
```

**GREEN: 구현**
```bash
claude "SlackIntegration 엔티티 구현해줘.
@Entity
@Table(name = \"slack_integrations\")

필드:
- id, user (@OneToOne LAZY, unique), webhookUrl
- morningNotification (default true)
- completionNotification (default false)
- eveningReport (default true)
- morningTime (default \"09:00\")
- eveningTime (default \"20:00\")
- enabled (default true)

메서드:
- updateSettings(Boolean morning, Boolean completion, Boolean evening)
- updateTimes(String morningTime, String eveningTime)
- enable() / disable()

위치: src/main/java/com/chronicle/domain/slack/SlackIntegration.java"
```

**✅ SlackIntegrationRepository**
```bash
claude "SlackIntegrationRepository 구현해줘.
- findByUserId(Long userId)
- findAllByEnabledTrue() - Scheduler용

위치: src/main/java/com/chronicle/domain/slack/SlackIntegrationRepository.java"
```

**4. N+1 최적화 쿼리 추가 (1-2시간)**

```bash
claude "DailyNoteRepository에 Fetch Join 쿼리 추가해줘.
@Query(\"\"\"
    SELECT DISTINCT d FROM DailyNote d
    LEFT JOIN FETCH d.tasks
    LEFT JOIN FETCH d.attachments
    WHERE d.id = :id
    \"\"\")
Optional<DailyNote> findByIdWithTasksAndAttachments(@Param(\"id\") Long id);

@Query(\"\"\"
    SELECT DISTINCT d FROM DailyNote d
    LEFT JOIN FETCH d.user
    LEFT JOIN FETCH d.tasks
    WHERE d.user.id = :userId
    AND d.date BETWEEN :startDate AND :endDate
    ORDER BY d.date DESC
    \"\"\")
List<DailyNote> findByUserIdAndDateRange(...);

테스트도 함께 작성

위치: 기존 DailyNoteRepository.java"
```

**✅ Day 2 완료 체크리스트**
- [ ] Task Entity + Repository 완성
- [ ] Attachment Entity + Repository 완성
- [ ] SlackIntegration Entity + Repository 완성
- [ ] Fetch Join 쿼리 추가
- [ ] 모든 도메인 테스트 Green
- [ ] Git commit (feat: add Task, Attachment, SlackIntegration domain)

---

### Day 3 (1/20, 월) - DTO 설계
**목표**: Request/Response DTO 설계  
**예상 시간**: 6-7시간

#### 오전 (3시간)
**1. DailyNote DTO (1.5시간)**

```bash
claude "DailyNote DTO 만들어줘.

1. DailyNoteRequest record
   - date (LocalDate) @NotNull
   - content (String) - nullable

2. DailyNoteResponse record
   - id, date, content, createdAt, updatedAt
   - from(DailyNote) 정적 팩토리

3. DailyNoteDetailResponse record (작업 포함)
   - id, date, content, tasks (List<TaskResponse>), attachments, createdAt
   - from(DailyNote)

4. DailyNoteSummaryResponse record (캘린더용)
   - date, taskCount, completedCount
   - from(DailyNote)

@Schema 어노테이션 포함
위치: src/main/java/com/chronicle/dto/dailynote/"
```

**2. Task DTO (1.5시간)**

```bash
claude "Task DTO 만들어줘.

1. TaskRequest record
   - title @NotBlank @Length(max=200)
   - position (Integer) - nullable

2. TaskResponse record
   - id, title, completed, position, createdAt
   - from(Task)

3. TaskUpdateRequest record
   - title (nullable)
   - position (nullable)

위치: src/main/java/com/chronicle/dto/task/"
```

#### 오후 (3-4시간)
**3. User & Auth DTO (1.5시간)**

```bash
claude "User & Auth DTO 만들어줘.

1. UserRegisterRequest record
   - email @Email @NotBlank
   - password @NotBlank @Length(min=8)
   - name @NotBlank

2. UserResponse record
   - id, email, name, role, createdAt
   - from(User)

3. LoginRequest record
   - email @Email @NotBlank
   - password @NotBlank

4. TokenResponse record
   - accessToken, refreshToken, tokenType (\"Bearer\")

위치: src/main/java/com/chronicle/dto/user/, dto/auth/"
```

**4. Attachment & Slack DTO (1.5-2시간)**

```bash
claude "Attachment DTO 만들어줘.

1. AttachmentResponse record
   - id, fileName, fileUrl, fileType, fileSize, uploadedAt
   - from(Attachment)

위치: src/main/java/com/chronicle/dto/attachment/"

claude "Slack DTO 만들어줘.

1. SlackConnectRequest record
   - webhookUrl @NotBlank

2. SlackSettingsRequest record
   - morningNotification, completionNotification, eveningReport
   - morningTime, eveningTime

3. SlackResponse record
   - enabled, morningNotification, completionNotification
   - eveningReport, morningTime, eveningTime
   - from(SlackIntegration)

4. SlackMessage record (Slack API용)
   - text, attachments (List<SlackAttachment>)

5. DailyReport record (리포트용)
   - totalTasks, completedTasks, completedTaskTitles, incompleteTaskTitles
   - completionPercentage()

위치: src/main/java/com/chronicle/dto/slack/"
```

**5. PageRequest/PageResult DTO (30분)**

```bash
claude "페이징 DTO 만들어줘.

1. PageRequestDto record
   - page (default 0), size (default 10)
   - toPageable()

2. PageResultDto<T> record
   - content, totalPages, totalElements, number, size, first, last
   - of(Page<T> page)

위치: src/main/java/com/chronicle/common/dto/"
```

**✅ Day 3 완료 체크리스트**
- [ ] DailyNote DTO 3종
- [ ] Task DTO
- [ ] User & Auth DTO
- [ ] Attachment & Slack DTO
- [ ] 페이징 DTO
- [ ] Git commit (feat: add DTOs)

---

### Day 4 (1/21, 화) - DailyNoteService TDD
**목표**: DailyNote 비즈니스 로직 구현  
**예상 시간**: 7-8시간

#### 오전 (4시간)
**1. DailyNoteService 생성/조회 TDD (2.5시간)**

**RED: 테스트**
```bash
claude "DailyNoteService 테스트 작성해줘.
@ExtendWith(MockitoExtension.class)
@Mock: DailyNoteRepository, UserRepository
@InjectMocks: DailyNoteService

테스트 케이스:
- 노트_생성_성공
  - 기존 노트 없으면 새로 생성
- 노트_조회_성공 (user + date)
  - 존재하면 조회, 없으면 빈 노트 생성해서 반환
- 특정_날짜_노트_조회_존재하지_않으면_생성
- 존재하지_않는_사용자로_생성_시_예외

위치: src/test/java/com/chronicle/service/DailyNoteServiceTest.java"
```

**GREEN: 구현**
```bash
claude "DailyNoteService 구현해줘.
@Service
@Transactional
@RequiredArgsConstructor

메서드:
- getOrCreateDailyNote(Long userId, LocalDate date) → DailyNoteResponse
  1. userId + date로 조회
  2. 없으면 빈 노트 생성
  3. DailyNoteResponse 반환

- getDailyNoteDetail(Long userId, LocalDate date) → DailyNoteDetailResponse
  1. Fetch Join 쿼리로 조회 (tasks, attachments 포함)
  2. 없으면 빈 노트 생성

위치: src/main/java/com/chronicle/service/DailyNoteService.java"
```

**✅ Custom Exception**
```bash
claude "Custom Exception 만들어줘.

1. BusinessException extends RuntimeException
   - ErrorCode 필드

2. UserNotFoundException extends BusinessException
3. DailyNoteNotFoundException extends BusinessException

위치: src/main/java/com/chronicle/exception/"
```

**2. DailyNoteService 수정/삭제 TDD (1.5시간)**

**RED: 테스트**
```bash
claude "DailyNoteService 수정/삭제 테스트 추가해줘.
테스트 케이스:
- 노트_내용_수정_성공
- 소유자가_아닌_사용자_수정_시_예외 (ForbiddenException)
- 노트_삭제_성공
- 소유자가_아닌_사용자_삭제_시_예외

위치: 기존 DailyNoteServiceTest.java"
```

**GREEN: 구현**
```bash
claude "DailyNoteService 수정/삭제 메서드 추가해줘.
메서드:
- updateDailyNote(Long userId, LocalDate date, DailyNoteRequest request)
  1. 노트 조회
  2. 소유자 확인
  3. updateContent
  
- deleteDailyNote(Long userId, LocalDate date)
  1. 노트 조회
  2. 소유자 확인
  3. delete

위치: 기존 DailyNoteService.java"
```

#### 오후 (3-4시간)
**3. 캘린더 데이터 조회 TDD (2시간)**

**RED: 테스트**
```bash
claude "캘린더 데이터 조회 테스트 추가해줘.
테스트 케이스:
- 월간_노트_요약_조회 (2025-01)
  - 해당 월의 모든 노트 반환
  - 각 노트의 taskCount, completedCount 포함

위치: 기존 DailyNoteServiceTest.java"
```

**GREEN: 구현**
```bash
claude "캘린더 데이터 조회 메서드 추가해줘.
메서드:
- getMonthlyNotes(Long userId, YearMonth yearMonth) → List<DailyNoteSummaryResponse>
  1. startDate = yearMonth.atDay(1)
  2. endDate = yearMonth.atEndOfMonth()
  3. findByUserIdAndDateRange로 조회
  4. DailyNoteSummaryResponse로 변환

위치: 기존 DailyNoteService.java"
```

**4. 통합 테스트 (1-2시간)**

```bash
claude "DailyNoteService 통합 테스트 작성해줘.
@SpringBootTest
@Transactional

시나리오:
- 노트 생성 → 조회 → 수정 → 조회 → 삭제
- 월간 데이터 조회

위치: src/test/java/com/chronicle/service/DailyNoteServiceIntegrationTest.java"
```

**✅ Day 4 완료 체크리스트**
- [ ] DailyNoteService 생성/조회/수정/삭제 완성
- [ ] Custom Exception 구현
- [ ] 캘린더 데이터 조회 기능
- [ ] 통합 테스트 통과
- [ ] 모든 테스트 Green
- [ ] Git commit (feat: add DailyNoteService)

---

### Day 5 (1/22, 수) - TaskService TDD
**목표**: Task 비즈니스 로직 구현  
**예상 시간**: 7-8시간

#### 오전 (4시간)
**1. TaskService 생성/조회 TDD (2.5시간)**

**RED: 테스트**
```bash
claude "TaskService 테스트 작성해줘.
@Mock: TaskRepository, DailyNoteRepository
@InjectMocks: TaskService

테스트 케이스:
- 작업_생성_성공
  - dailyNoteId + TaskRequest
  - position이 null이면 마지막 순서로 설정
- 존재하지_않는_노트에_작업_생성_시_예외
- 특정_노트의_작업_목록_조회 (position 순)

위치: src/test/java/com/chronicle/service/TaskServiceTest.java"
```

**GREEN: 구현**
```bash
claude "TaskService 구현해줘.
@Service
@Transactional
@RequiredArgsConstructor

메서드:
- createTask(Long dailyNoteId, TaskRequest request) → TaskResponse
  1. DailyNote 조회
  2. position이 null이면 현재 최대값 + 1
  3. Task 생성 및 저장
  4. TaskResponse 반환

- getTasksByDailyNote(Long dailyNoteId) → List<TaskResponse>
  - findByDailyNoteIdOrderByPosition

위치: src/main/java/com/chronicle/service/TaskService.java"
```

**2. TaskService 토글/수정/삭제 TDD (1.5시간)**

**RED: 테스트**
```bash
claude "TaskService 토글/수정/삭제 테스트 추가해줘.
테스트 케이스:
- 작업_완료_토글_성공
- 작업_제목_수정_성공
- 작업_순서_변경_성공
- 작업_삭제_성공
- 존재하지_않는_작업_토글_시_예외

위치: 기존 TaskServiceTest.java"
```

**GREEN: 구현**
```bash
claude "TaskService 토글/수정/삭제 메서드 추가해줘.
메서드:
- toggleTask(Long taskId) → TaskResponse
  1. Task 조회
  2. task.toggle()
  3. TaskResponse 반환

- updateTask(Long taskId, TaskUpdateRequest request)
  - title이나 position 업데이트

- deleteTask(Long taskId)

위치: 기존 TaskService.java"
```

#### 오후 (3-4시간)
**3. 오늘/월간 작업 조회 TDD (1.5시간)**

**RED: 테스트**
```bash
claude "오늘/월간 작업 조회 테스트 추가해줘.
테스트 케이스:
- 오늘_할_일_목록_조회 (userId, date)
- 월간_리포트_조회 (userId, yearMonth)
  - 완료/미완료 개수
  - 완료율

위치: 기존 TaskServiceTest.java"
```

**GREEN: 구현**
```bash
claude "오늘/월간 조회 메서드 추가해줘.
메서드:
- getTodayTasks(Long userId, LocalDate date) → List<TaskResponse>
  1. DailyNote 조회
  2. Tasks 반환

- getDailyReport(Long userId, LocalDate date) → DailyReport
  1. 오늘 작업 조회
  2. 완료/미완료 분류
  3. DailyReport 생성

위치: 기존 TaskService.java"
```

**4. 통합 테스트 (1.5-2시간)**

```bash
claude "TaskService 통합 테스트 작성해줘.
@SpringBootTest
@Transactional

시나리오:
- 노트 생성 → 작업 3개 생성 → 순서 변경 → 토글 → 삭제

위치: src/test/java/com/chronicle/service/TaskServiceIntegrationTest.java"
```

**✅ Day 5 완료 체크리스트**
- [ ] TaskService 전체 기능 완성
- [ ] 토글/수정/삭제 로직
- [ ] 오늘/월간 조회 기능
- [ ] 통합 테스트 통과
- [ ] Git commit (feat: add TaskService)

---

### Day 6 (1/23, 목) - DailyNoteController & TaskController TDD
**목표**: REST API 구현  
**예상 시간**: 6-7시간

#### 오전 (3시간)
**1. DailyNoteController TDD (2시간)**

**RED: 테스트**
```bash
claude "DailyNoteController 테스트 작성해줘.
@WebMvcTest(DailyNoteController.class)
@MockBean: DailyNoteService
@Autowired: MockMvc

테스트 케이스:
- GET_/api/daily-notes?date=2025-01-18_조회_성공_200
- PUT_/api/daily-notes?date=2025-01-18_수정_성공_200
- DELETE_/api/daily-notes?date=2025-01-18_삭제_성공_204
- GET_/api/daily-notes/calendar?month=2025-01_월간_데이터_200

위치: src/test/java/com/chronicle/controller/DailyNoteControllerTest.java"
```

**GREEN: 구현**
```bash
claude "DailyNoteController 구현해줘.
@RestController
@RequestMapping(\"/api/daily-notes\")
@RequiredArgsConstructor

엔드포인트:
GET /api/daily-notes?date={date}
  - getOrCreateDailyNote
  - ApiResponse<DailyNoteDetailResponse>

PUT /api/daily-notes?date={date}
  - @Valid @RequestBody DailyNoteRequest
  - updateDailyNote
  - ApiResponse<DailyNoteResponse>

DELETE /api/daily-notes?date={date}
  - deleteDailyNote
  - NO_CONTENT

GET /api/daily-notes/calendar?month={month}
  - getMonthlyNotes
  - ApiResponse<List<DailyNoteSummaryResponse>>

userId는 임시로 1L 하드코딩 (JWT 인증 전)

위치: src/main/java/com/chronicle/controller/DailyNoteController.java"
```

**2. TaskController TDD (1시간)**

**RED: 테스트**
```bash
claude "TaskController 테스트 작성해줘.
테스트 케이스:
- POST_/api/tasks_생성_성공_201
- GET_/api/daily-notes/{id}/tasks_목록_조회_200
- PATCH_/api/tasks/{id}/toggle_토글_200
- PUT_/api/tasks/{id}_수정_200
- DELETE_/api/tasks/{id}_삭제_204

위치: src/test/java/com/chronicle/controller/TaskControllerTest.java"
```

**GREEN: 구현**
```bash
claude "TaskController 구현해줘.
@RestController
@RequestMapping(\"/api/tasks\")

엔드포인트:
POST /api/tasks?dailyNoteId={id}
PATCH /api/tasks/{id}/toggle
PUT /api/tasks/{id}
DELETE /api/tasks/{id}

GET /api/daily-notes/{dailyNoteId}/tasks
  - getTasksByDailyNote

위치: src/main/java/com/chronicle/controller/TaskController.java"
```

#### 오후 (3-4시간)
**3. GlobalExceptionHandler (1.5시간)**

```bash
claude "GlobalExceptionHandler 구현해줘.
@RestControllerAdvice

처리할 예외:
- MethodArgumentNotValidException → 400
- DailyNoteNotFoundException → 404
- TaskNotFoundException → 404
- UserNotFoundException → 404
- ForbiddenException → 403
- BusinessException → 해당 ErrorCode status
- Exception → 500

ErrorResponse로 반환

위치: src/main/java/com/chronicle/exception/GlobalExceptionHandler.java"
```

**4. Swagger 설정 (1시간)**

```bash
claude "SwaggerConfig 구현해줘.
springdoc-openapi-starter-webmvc-ui

OpenAPI 설정:
- title: Chronicle API
- version: 1.0
- description: 실시간 개인 생산성 관리 API

위치: src/main/java/com/chronicle/config/SwaggerConfig.java"

claude "모든 Controller에 @Operation, @ApiResponse 추가해줘.
위치: controller 패키지"
```

**5. API 통합 테스트 (30분-1시간)**

```bash
claude "API 통합 테스트 작성해줘.
@SpringBootTest
@AutoConfigureMockMvc

시나리오:
- 노트 생성 → 작업 추가 → 토글 → 조회 → 삭제

위치: src/test/java/com/chronicle/integration/ApiIntegrationTest.java"
```

**✅ Day 6 완료 체크리스트**
- [ ] DailyNoteController 완성
- [ ] TaskController 완성
- [ ] GlobalExceptionHandler 완성
- [ ] Swagger 설정
- [ ] API 통합 테스트
- [ ] Postman 수동 테스트
- [ ] Git commit (feat: add Controllers and exception handling)

---

### Day 7 (1/24, 금) - UserService & Week 1 마무리
**목표**: User 관련 기능 및 1주차 정리  
**예상 시간**: 6-7시간

#### 오전 (3시간)
**1. UserService TDD (2시간)**

**RED: 테스트**
```bash
claude "UserService 테스트 작성해줘.
@Mock: UserRepository, PasswordEncoder
@InjectMocks: UserService

테스트 케이스:
- 회원가입_성공
- 중복_이메일_회원가입_시_예외 (DuplicateEmailException)
- 비밀번호_암호화_확인

위치: src/test/java/com/chronicle/service/UserServiceTest.java"
```

**GREEN: 구현**
```bash
claude "UserService 구현해줘.
메서드:
- register(UserRegisterRequest request) → UserResponse
  1. 이메일 중복 체크
  2. 비밀번호 암호화
  3. User 생성 (role = USER)
  4. save

위치: src/main/java/com/chronicle/service/UserService.java"
```

**✅ SecurityConfig (기본)**
```bash
claude "기본 SecurityConfig 만들어줘.
- PasswordEncoder Bean (BCryptPasswordEncoder)
- CSRF 비활성화
- 모든 요청 permitAll (임시)

위치: src/main/java/com/chronicle/security/SecurityConfig.java"
```

**2. AuthController (회원가입만) (1시간)**

```bash
claude "AuthController 회원가입 API 구현해줘.
POST /api/auth/register
  - UserRegisterRequest
  - UserService.register
  - ApiResponse<UserResponse>

위치: src/main/java/com/chronicle/controller/AuthController.java"
```

#### 오후 (3-4시간)
**3. Week 1 총정리 (3-4시간)**

**✅ 전체 테스트 실행**
```bash
./gradlew clean test
```

**✅ 테스트 커버리지 확인**
```bash
./gradlew test jacocoTestReport
open build/reports/jacoco/test/html/index.html

목표: 70% 이상
```

**✅ 코드 리뷰 및 리팩토링**
- [ ] 중복 코드 제거
- [ ] 메서드 네이밍 개선
- [ ] 불필요한 주석 정리
- [ ] Magic Number 상수화

**✅ README 업데이트**
```markdown
# Chronicle

## 진행 상황 (Week 1)
- [x] 도메인 모델 완성 (User, DailyNote, Task, Attachment, SlackIntegration)
- [x] Service 레이어 (DailyNote, Task, User)
- [x] Controller 레이어 (DailyNote, Task, Auth)
- [x] 예외 처리
- [x] Swagger 설정
- [x] 테스트 커버리지 70%+

## 다음 주 계획
- JWT 인증
- WebSocket 실시간 동기화
- Slack API 연동
```

**✅ Week 1 완료 체크리스트**
- [ ] User/DailyNote/Task 도메인 완성
- [ ] Service 레이어 완성
- [ ] Controller 레이어 완성
- [ ] 예외 처리 완성
- [ ] Swagger 문서
- [ ] 테스트 커버리지 70%+
- [ ] 모든 테스트 Green
- [ ] Git commit (docs: complete Week 1)

---

## 📅 Week 2: 인증 & WebSocket & Slack (7일)

### Day 8 (1/25, 토) - JWT 인증 기본
**목표**: JWT 생성 및 Security 설정  
**예상 시간**: 7-8시간

#### 오전 (4시간)
**1. JWT 설정 (1.5시간)**

**application.yml**
```yaml
jwt:
  secret: ${JWT_SECRET:your-secret-key-must-be-at-least-256-bits-long-change-in-production}
  access-token-expiration: 3600000  # 1시간
  refresh-token-expiration: 604800000  # 7일
```

**✅ JwtProperties**
```bash
claude "JwtProperties 만들어줘.
@ConfigurationProperties(\"jwt\")
@Component

위치: src/main/java/com/chronicle/security/JwtProperties.java"
```

**✅ JwtTokenProvider TDD**
```bash
claude "JwtTokenProvider 테스트 작성해줘.
테스트 케이스:
- Access_Token_생성_성공
- Refresh_Token_생성_성공
- 유효한_토큰_검증_성공
- 만료된_토큰_검증_실패
- 토큰에서_userId_추출_성공

위치: src/test/java/com/chronicle/security/JwtTokenProviderTest.java"

claude "JwtTokenProvider 구현해줘.
@Component

메서드:
- generateAccessToken(userId, email, role)
- generateRefreshToken(userId)
- validateToken(token)
- getUserIdFromToken(token)

io.jsonwebtoken 사용

위치: src/main/java/com/chronicle/security/JwtTokenProvider.java"
```

**2. 로그인 기능 TDD (2.5시간)**

**✅ LoginRequest, TokenResponse DTO (이미 Day 3에 생성됨)**

**RED: UserService 로그인 테스트**
```bash
claude "UserService 로그인 테스트 추가해줘.
테스트 케이스:
- 로그인_성공 (비밀번호 매칭)
- 존재하지_않는_이메일_로그인_실패
- 잘못된_비밀번호_로그인_실패 (InvalidPasswordException)

위치: 기존 UserServiceTest.java"
```

**GREEN: 구현**
```bash
claude "UserService 로그인 메서드 추가해줘.
메서드:
- login(LoginRequest request) → TokenResponse
  1. email로 User 조회
  2. 비밀번호 매칭 (passwordEncoder.matches)
  3. Access/Refresh Token 생성
  4. TokenResponse 반환

위치: 기존 UserService.java"
```

**✅ InvalidPasswordException**
```bash
claude "InvalidPasswordException 만들어줘.
extends BusinessException
ErrorCode.UNAUTHORIZED

위치: src/main/java/com/chronicle/exception/"
```

#### 오후 (3-4시간)
**3. AuthController 로그인 API (1시간)**

**RED: 테스트**
```bash
claude "AuthController 로그인 테스트 추가해줘.
POST /api/auth/login 테스트

위치: 기존 AuthControllerTest.java"
```

**GREEN: 구현**
```bash
claude "AuthController 로그인 API 추가해줘.
POST /api/auth/login
  - LoginRequest
  - UserService.login
  - TokenResponse

위치: 기존 AuthController.java"
```

**4. JwtAuthenticationFilter TDD (2-3시간)**

**RED: 테스트**
```bash
claude "JwtAuthenticationFilter 테스트 작성해줘.
@SpringBootTest
@AutoConfigureMockMvc

테스트 케이스:
- 유효한_토큰으로_인증된_요청_성공
- 토큰_없이_보호된_API_접근_401
- 만료된_토큰_401

위치: src/test/java/com/chronicle/security/JwtAuthenticationFilterTest.java"
```

**GREEN: 구현**
```bash
claude "JwtAuthenticationFilter 구현해줘.
extends OncePerRequestFilter

doFilterInternal:
1. Authorization 헤더에서 토큰 추출
2. 토큰 검증
3. userId 추출
4. UsernamePasswordAuthenticationToken 생성
5. SecurityContextHolder 설정

위치: src/main/java/com/chronicle/security/JwtAuthenticationFilter.java"
```

**5. SecurityConfig 업데이트 (30분)**

```bash
claude "SecurityConfig 수정해줘.
- JwtAuthenticationFilter 추가
- 인증 제외: /api/auth/**, /swagger-ui/**, /v3/api-docs/**
- 나머지는 인증 필요
- CORS 설정

위치: 기존 SecurityConfig.java"
```

**✅ Day 8 완료 체크리스트**
- [ ] JWT 생성/검증 로직
- [ ] 로그인 기능
- [ ] JwtAuthenticationFilter
- [ ] SecurityConfig 설정
- [ ] 인증 통합 테스트
- [ ] Git commit (feat: add JWT authentication)

---

### Day 9 (1/26, 일) - 인증 완성 & Controller 수정
**목표**: JWT 인증 적용 및 userId 추출  
**예상 시간**: 6-7시간

#### 오전 (3시간)
**1. @AuthenticationPrincipal 활용 (2시간)**

**✅ UserPrincipal 클래스**
```bash
claude "UserPrincipal 클래스 만들어줘.
- userId (Long)
- email, role
- 생성자

위치: src/main/java/com/chronicle/security/UserPrincipal.java"
```

**✅ Controller 수정 (userId 하드코딩 제거)**
```bash
claude "모든 Controller 수정해줘.
기존: Long userId = 1L; (하드코딩)
변경: @AuthenticationPrincipal UserPrincipal principal
      Long userId = principal.getUserId();

DailyNoteController, TaskController 수정

위치: controller 패키지"
```

**2. 인증 통합 테스트 (1시간)**

```bash
claude "인증 통합 테스트 작성해줘.
@SpringBootTest
@AutoConfigureMockMvc

시나리오:
1. 회원가입
2. 로그인 (토큰 받기)
3. 토큰으로 노트 생성
4. 토큰으로 작업 추가
5. 토큰 없이 요청 → 401

위치: src/test/java/com/chronicle/integration/AuthIntegrationTest.java"
```

#### 오후 (3-4시간)
**3. Swagger JWT 설정 (1시간)**

```bash
claude "SwaggerConfig에 JWT 설정 추가해줘.
- securitySchemes (Bearer Token)
- securityRequirement

위치: 기존 SwaggerConfig.java"
```

**4. 전체 API 테스트 (2-3시간)**
- [ ] Swagger UI에서 모든 API 테스트
- [ ] JWT 토큰으로 인증 확인
- [ ] Postman Collection 생성
- [ ] 버그 수정

**✅ Day 9 완료 체크리스트**
- [ ] @AuthenticationPrincipal 적용
- [ ] Controller userId 추출
- [ ] 인증 통합 테스트
- [ ] Swagger JWT 설정
- [ ] 전체 API 동작 확인
- [ ] Git commit (feat: complete JWT authentication)

---

### Day 10 (1/27, 월) - WebSocket 설정
**목표**: WebSocket 실시간 동기화 기본 구현  
**예상 시간**: 7-8시간

#### 오전 (3-4시간)
**1. WebSocket 설정 (2시간)**

**✅ WebSocketConfig**
```bash
claude "WebSocketConfig 구현해줘.
@Configuration
@EnableWebSocketMessageBroker

configureMessageBroker:
- enableSimpleBroker(\"/topic\")
- setApplicationDestinationPrefixes(\"/app\")

registerStompEndpoints:
- addEndpoint(\"/ws\")
- setAllowedOriginPatterns(\"*\")
- withSockJS()

위치: src/main/java/com/chronicle/websocket/WebSocketConfig.java"
```

**✅ WebSocketEventListener**
```bash
claude "WebSocketEventListener 구현해줘.
@Component
@Slf4j

@EventListener:
- SessionConnectedEvent → 로그
- SessionDisconnectEvent → 로그

위치: src/main/java/com/chronicle/websocket/WebSocketEventListener.java"
```

**2. WebSocket 메시지 DTO (1-2시간)**

```bash
claude "WebSocket 메시지 DTO 만들어줘.

1. TaskUpdateMessage record
   - type (CREATED, UPDATED, DELETED, TOGGLE)
   - taskId, dailyNoteId, date
   - task (TaskResponse)
   - userId, timestamp
   - 정적 팩토리: of(Task task, String type)

2. NoteUpdateMessage record
   - type (UPDATED)
   - dailyNoteId, date, content
   - userId, timestamp

위치: src/main/java/com/chronicle/dto/websocket/"
```

#### 오후 (4시간)
**3. TaskService에 WebSocket 통합 (2.5시간)**

```bash
claude "TaskService 수정해줘.
@Autowired SimpMessagingTemplate 추가

각 메서드에서 WebSocket 메시지 전송:
- createTask → CREATED 메시지
- toggleTask → TOGGLE 메시지
- updateTask → UPDATED 메시지
- deleteTask → DELETED 메시지

메시지 전송:
messagingTemplate.convertAndSend(
    \"/topic/user/\" + userId,
    TaskUpdateMessage.of(task, \"CREATED\")
);

위치: 기존 TaskService.java"
```

**4. WebSocket 테스트 (1.5시간)**

```bash
claude "WebSocket 테스트 작성해줘.
@SpringBootTest(webEnvironment = RANDOM_PORT)
StompSession 사용

테스트 케이스:
- WebSocket_연결_성공
- 작업_생성_시_메시지_수신
- 작업_토글_시_메시지_수신

위치: src/test/java/com/chronicle/websocket/WebSocketTest.java"
```

**✅ Day 10 완료 체크리스트**
- [ ] WebSocket 설정 완료
- [ ] TaskService WebSocket 통합
- [ ] 실시간 메시지 전송 확인
- [ ] WebSocket 테스트 통과
- [ ] Git commit (feat: add WebSocket real-time sync)

---

### Day 11 (1/28, 화) - Slack API 연동
**목표**: Slack 알림 기능 구현  
**예상 시간**: 7-8시간

#### 오전 (4시간)
**1. SlackService 구현 (2.5시간)**

**✅ Slack DTO (이미 Day 3에 생성)**

**✅ SlackService TDD**
```bash
claude "SlackService 테스트 작성해줘.
@Mock: RestTemplate
@InjectMocks: SlackService

테스트 케이스:
- 아침_할_일_전송_성공
- 저녁_리포트_전송_성공
- Webhook_URL_오류_시_예외_처리

위치: src/test/java/com/chronicle/service/SlackServiceTest.java"

claude "SlackService 구현해줘.
@Service
@RequiredArgsConstructor

메서드:
- sendMorningTasks(webhookUrl, tasks, date)
  - Slack 메시지 포맷팅
  - RestTemplate.postForEntity

- sendEveningReport(webhookUrl, report, date)
  - 완료/미완료 포맷팅
  - 전송

- sendTestMessage(webhookUrl)
  - 테스트용

private 메서드:
- formatDate(LocalDate) → \"2025년 1월 18일 토요일\"
- formatTasks(List<Task>)

위치: src/main/java/com/chronicle/service/SlackService.java"
```

**2. SlackIntegrationService TDD (1.5시간)**

```bash
claude "SlackIntegrationService 테스트 및 구현해줘.
메서드:
- connectSlack(userId, webhookUrl) → SlackResponse
- updateSettings(userId, request)
- disconnect(userId)
- getSettings(userId) → SlackResponse

위치: src/test/.../service/, src/main/.../service/"
```

#### 오후 (3-4시간)
**3. SlackController TDD (1.5시간)**

```bash
claude "SlackController 테스트 및 구현해줘.
@RestController
@RequestMapping(\"/api/slack\")

엔드포인트:
POST /api/slack/connect
PUT /api/slack/settings
POST /api/slack/test
DELETE /api/slack/disconnect
GET /api/slack/settings

위치: src/test/.../controller/, src/main/.../controller/"
```

**4. Spring Scheduler 구현 (1.5-2시간)**

**✅ Scheduler 활성화**
```yaml
# application.yml
spring:
  task:
    scheduling:
      pool:
        size: 5
```

**✅ SlackNotificationScheduler**
```bash
claude "SlackNotificationScheduler 구현해줘.
@Component
@RequiredArgsConstructor
@EnableScheduling
@Slf4j

메서드:
- sendMorningNotifications()
  @Scheduled(cron = \"0 0 9 * * *\")  # 매일 9시
  1. enabled=true인 모든 SlackIntegration 조회
  2. morningNotification=true인 사용자만
  3. 오늘 할 일 조회
  4. SlackService.sendMorningTasks

- sendEveningReports()
  @Scheduled(cron = \"0 0 20 * * *\")  # 매일 8시
  1. eveningReport=true인 사용자
  2. DailyReport 생성
  3. SlackService.sendEveningReport

에러 처리: try-catch로 개별 사용자 실패해도 계속 진행

위치: src/main/java/com/chronicle/scheduler/SlackNotificationScheduler.java"
```

**5. Scheduler 테스트 (30분)**

```bash
# 수동 테스트용 API 추가
POST /api/slack/trigger-morning  # 수동으로 아침 알림 트리거
POST /api/slack/trigger-evening  # 수동으로 저녁 알림 트리거
```

**✅ Day 11 완료 체크리스트**
- [ ] SlackService 구현
- [ ] SlackIntegrationService 구현
- [ ] SlackController 구현
- [ ] Scheduler 설정 및 구현
- [ ] 테스트 Slack Webhook으로 실제 알림 확인
- [ ] Git commit (feat: add Slack integration and scheduler)

---

### Day 12 (1/29, 수) - S3 파일 업로드
**목표**: AWS S3 파일 업로드/다운로드  
**예상 시간**: 6-7시간

#### 오전 (3시간)
**1. S3 설정 (1시간)**

**application.yml**
```yaml
aws:
  s3:
    bucket: chronicle-attachments
    region: ap-northeast-2
  access-key: ${AWS_ACCESS_KEY}
  secret-key: ${AWS_SECRET_KEY}
```

**✅ S3Config**
```bash
claude "S3Config 구현해줘.
@Configuration

@Bean AmazonS3:
- AWSCredentials
- AmazonS3ClientBuilder

위치: src/main/java/com/chronicle/config/S3Config.java"
```

**2. AttachmentService TDD (2시간)**

**RED: 테스트**
```bash
claude "AttachmentService 테스트 작성해줘.
@Mock: AmazonS3, AttachmentRepository, DailyNoteRepository
@InjectMocks: AttachmentService

테스트 케이스:
- 파일_업로드_성공
- 파일명_중복_방지_확인 (UUID)
- S3_업로드_실패_시_예외
- 파일_삭제_성공

위치: src/test/java/com/chronicle/service/AttachmentServiceTest.java"
```

**GREEN: 구현**
```bash
claude "AttachmentService 구현해줘.
@Service
@Transactional

메서드:
- uploadFile(dailyNoteId, MultipartFile file) → AttachmentResponse
  1. 파일명 중복 방지 (UUID + extension)
  2. S3 업로드 (ObjectMetadata 설정)
  3. S3 URL 생성
  4. Attachment 엔티티 생성 및 저장

- deleteFile(attachmentId)
  1. Attachment 조회
  2. S3에서 삭제
  3. DB에서 삭제

- getAttachments(dailyNoteId) → List<AttachmentResponse>

위치: src/main/java/com/chronicle/service/AttachmentService.java"
```

#### 오후 (3-4시간)
**3. AttachmentController TDD (1.5시간)**

```bash
claude "AttachmentController 구현해줘.
@RestController
@RequestMapping(\"/api/attachments\")

엔드포인트:
POST /api/attachments?dailyNoteId={id}
  - @RequestParam MultipartFile file
  - uploadFile
  - CREATED

DELETE /api/attachments/{id}
  - deleteFile
  - NO_CONTENT

GET /api/daily-notes/{dailyNoteId}/attachments
  - getAttachments

위치: src/test/.../controller/, src/main/.../controller/"
```

**4. 파일 업로드 테스트 (1.5-2시간)**
- [ ] Postman으로 실제 파일 업로드
- [ ] S3 버킷에 파일 저장 확인
- [ ] 파일 URL로 다운로드 확인
- [ ] 파일 삭제 확인

**✅ Day 12 완료 체크리스트**
- [ ] S3 설정 완료
- [ ] AttachmentService 구현
- [ ] AttachmentController 구현
- [ ] 실제 S3 업로드/다운로드 확인
- [ ] Git commit (feat: add S3 file upload)

---

### Day 13 (1/30, 목) - Redis 캐싱 & N+1 최적화
**목표**: 성능 최적화  
**예상 시간**: 6-7시간

#### 오전 (3시간)
**1. Redis 설정 (1시간)**

**application.yml**
```yaml
spring:
  redis:
    host: localhost
    port: 6379
  cache:
    type: redis
    redis:
      time-to-live: 600000  # 10분
```

**✅ RedisCacheConfig**
```bash
claude "RedisCacheConfig 구현해줘.
@Configuration
@EnableCaching

RedisCacheManager:
- 기본 TTL 10분
- JSON 직렬화

위치: src/main/java/com/chronicle/config/RedisCacheConfig.java"
```

**2. 캐싱 적용 (2시간)**

```bash
claude "DailyNoteService에 캐싱 적용해줘.
@Cacheable(value = \"dailyNotes\", key = \"#userId + ':' + #date\")
public DailyNoteDetailResponse getDailyNoteDetail(...)

@CacheEvict(value = \"dailyNotes\", key = \"#userId + ':' + #date\")
public void updateDailyNote(...)

@CacheEvict(value = \"dailyNotes\", key = \"#userId + ':' + #date\")
public void deleteDailyNote(...)

위치: 기존 DailyNoteService.java"
```

**✅ 캐싱 테스트**
```bash
claude "캐싱 테스트 작성해줘.
@SpringBootTest
Redis 실행 필요

테스트 케이스:
- 첫_조회_시_DB_접근
- 두번째_조회_시_캐시_사용 (repository 호출 안 함)
- 수정_시_캐시_무효화

위치: src/test/java/com/chronicle/service/DailyNoteServiceCacheTest.java"
```

#### 오후 (3-4시간)
**3. N+1 쿼리 최적화 검증 (2시간)**

```bash
# application.yml - 쿼리 로그
logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

**✅ N+1 테스트**
```bash
claude "N+1 문제 확인 테스트 작성해줘.
@DataJpaTest

테스트:
- findByIdWithTasksAndAttachments 사용 시 쿼리 1-2개
- 일반 findById 사용 시 N+1 발생 확인

위치: src/test/java/com/chronicle/performance/NPlusOneTest.java"
```

**4. 성능 측정 (1-2시간)**

```bash
claude "성능 테스트 작성해줘.
@SpringBootTest

테스트:
- 캐싱_적용_전후_응답시간_비교
- N+1_해결_전후_쿼리_개수_비교

StopWatch 사용

위치: src/test/java/com/chronicle/performance/PerformanceTest.java"
```

**✅ Day 13 완료 체크리스트**
- [ ] Redis 캐싱 적용
- [ ] 캐싱 테스트 통과
- [ ] N+1 쿼리 0건 확인
- [ ] 성능 개선 수치화
- [ ] Git commit (perf: add Redis caching and optimize queries)

---

### Day 14 (1/31, 금) - Week 2 마무리 & 리팩토링
**목표**: 코드 품질 개선 및 문서화  
**예상 시간**: 6-7시간

#### 오전 (3시간)
**1. 코드 리뷰 및 리팩토링 (2시간)**
- [ ] 중복 코드 제거
- [ ] 메서드 네이밍 개선
- [ ] 불필요한 주석 제거
- [ ] Magic Number 상수화
- [ ] 테스트 코드 정리

**2. 테스트 커버리지 보완 (1시간)**
```bash
./gradlew test jacocoTestReport
```
- [ ] 커버리지 80% 미만인 클래스 테스트 추가

#### 오후 (3-4시간)
**3. E2E 통합 테스트 (2시간)**

```bash
claude "E2E 시나리오 통합 테스트 작성해줘.
@SpringBootTest
@AutoConfigureMockMvc

시나리오:
1. 회원가입
2. 로그인 (JWT 토큰)
3. 오늘 노트 생성
4. 작업 3개 추가
5. WebSocket 연결
6. 작업 토글 (실시간 메시지 확인)
7. 파일 업로드
8. Slack 연동 설정
9. 월간 캘린더 조회

위치: src/test/java/com/chronicle/integration/E2ETest.java"
```

**4. README 업데이트 (1-2시간)**

```markdown
# Chronicle - 실시간 개인 생산성 관리 앱

## 프로젝트 소개
...

## 기술 스택
...

## 주요 기능
- 날짜 기반 ToDo + 마크다운 노트
- WebSocket 실시간 동기화
- Slack 자동 알림
- S3 파일 업로드
- 캘린더 뷰

## 진행 상황 (Week 2 완료)
- [x] JWT 인증/인가
- [x] WebSocket 실시간 동기화
- [x] Slack API 연동 + Scheduler
- [x] S3 파일 업로드
- [x] Redis 캐싱
- [x] N+1 최적화
- [x] 테스트 커버리지 80%+

## 성능 개선
- N+1 쿼리 제거 → 쿼리 개수 70% 감소
- Redis 캐싱 → 응답시간 50% 개선

## API 문서
http://localhost:8080/swagger-ui.html

## 실행 방법
...
```

**✅ Week 2 완료 체크리스트**
- [ ] JWT 인증 완성
- [ ] WebSocket 실시간 동기화
- [ ] Slack 연동 + Scheduler
- [ ] S3 파일 업로드
- [ ] Redis 캐싱
- [ ] N+1 최적화
- [ ] 테스트 커버리지 80%+
- [ ] E2E 테스트 통과
- [ ] README 업데이트
- [ ] Git commit (docs: complete Week 2)

---

## 📅 Week 3: 프론트엔드 & 배포 (7일)

### Day 15-16 (2/1-2, 토-일) - React 프론트엔드
**목표**: React 프로젝트 세팅 및 기본 UI  
**예상 시간**: 12-14시간 (양일 합산)

#### Day 15 오전 (3시간)
**1. React 프로젝트 생성 (1시간)**
```bash
npm create vite@latest chronicle-frontend -- --template react-ts
cd chronicle-frontend
npm install
npm install axios react-router-dom zustand @tanstack/react-query
npm install @mui/material @emotion/react @emotion/styled
npm install @stomp/stompjs sockjs-client
npm install react-markdown toast-ui/editor
npm install react-calendar
```

**2. 폴더 구조 (2시간)**
```
src/
├── api/           # API 호출 (axios)
├── components/    # 공통 컴포넌트
├── pages/         # 페이지
├── hooks/         # Custom Hooks
├── stores/        # Zustand 상태 관리
├── types/         # TypeScript 타입
└── utils/         # 유틸리티
```

#### Day 15 오후 ~ Day 16 (9-11시간)
**3. 페이지 구현**
- [ ] 로그인/회원가입 페이지 (2시간)
- [ ] 메인 화면 (날짜 + ToDo + 메모) (4-5시간)
- [ ] 캘린더 팝업 (2시간)
- [ ] 설정 페이지 (Slack 연동) (2-3시간)

**✅ Day 15-16 완료 체크리스트**
- [ ] React 프로젝트 세팅
- [ ] 로그인/회원가입 UI
- [ ] 메인 화면 UI
- [ ] 캘린더 UI
- [ ] API 연동

---

### Day 17 (2/3, 월) - WebSocket & 마크다운 에디터
**목표**: 실시간 동기화 및 에디터 연동  
**예상 시간**: 6-7시간

#### 오전 (3시간)
**1. WebSocket 연결 (2시간)**
```typescript
// useWebSocket.ts
export const useWebSocket = (userId: number) => {
  const client = new Client({
    brokerURL: 'ws://localhost:8080/ws',
    onConnect: () => {
      client.subscribe('/topic/user/' + userId, (message) => {
        // 실시간 업데이트
      });
    }
  });
};
```

**2. 실시간 UI 업데이트 (1시간)**
- [ ] 작업 체크 시 즉시 반영
- [ ] 다른 디바이스에서 변경 시 동기화

#### 오후 (3-4시간)
**3. 마크다운 에디터 (2-3시간)**
- [ ] Toast UI Editor 연동
- [ ] 마크다운 렌더링
- [ ] 이미지 첨부

**4. 파일 업로드 UI (1시간)**
- [ ] 파일 선택
- [ ] 업로드 진행률
- [ ] 미리보기

**✅ Day 17 완료 체크리스트**
- [ ] WebSocket 연결
- [ ] 실시간 동기화 확인
- [ ] 마크다운 에디터
- [ ] 파일 업로드 UI

---

### Day 18-19 (2/4-5, 화-수) - 배포 준비
**목표**: Docker & AWS 배포  
**예상 시간**: 12-14시간

#### Day 18 오전 (3시간)
**1. Dockerfile 작성 (2시간)**
```dockerfile
# Multi-stage build
FROM gradle:8.5-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle clean build -x test

FROM openjdk:21-slim
COPY --from=build /app/build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

**2. docker-compose (1시간)**
```yaml
services:
  app:
    build: .
    ports:
      - "8080:8080"
    depends_on:
      - db
      - redis
  db:
    image: postgres:15
  redis:
    image: redis:7
  frontend:
    build: ./chronicle-frontend
    ports:
      - "5173:5173"
```

#### Day 18 오후 ~ Day 19 (9-11시간)
**3. AWS 배포 (5-6시간)**
- [ ] EC2 인스턴스 생성
- [ ] Docker 설치
- [ ] RDS PostgreSQL 연결
- [ ] ElastiCache Redis 연결
- [ ] S3 버킷 생성
- [ ] Nginx 설정
- [ ] SSL (Let's Encrypt)

**4. GitHub Actions CI/CD (3-4시간)**
```yaml
name: CI/CD
on:
  push:
    branches: [main]
jobs:
  build-and-deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Build Docker
      - name: Deploy to EC2
```

**5. 환경변수 설정 (1시간)**
- [ ] AWS Systems Manager
- [ ] JWT Secret
- [ ] DB 비밀번호
- [ ] S3 Access Key

**✅ Day 18-19 완료 체크리스트**
- [ ] Dockerfile 작성
- [ ] docker-compose 동작
- [ ] EC2 배포 성공
- [ ] CI/CD 파이프라인
- [ ] HTTPS 설정

---

### Day 20 (2/6, 목) - 성능 테스트 & 모니터링
**목표**: 성능 검증  
**예상 시간**: 6-7시간

#### 오전 (3시간)
**1. 부하 테스트 (2시간)**
```bash
# k6 사용
claude "k6 부하 테스트 스크립트 작성해줘.
- 동시 사용자 100명
- API 테스트

위치: performance/load-test.js"
```

**2. 성능 확인 (1시간)**
- [ ] 평균 응답시간 측정
- [ ] 에러율 확인
- [ ] 동시 접속 테스트

#### 오후 (3-4시간)
**3. 모니터링 설정 (2-3시간)**
- [ ] Spring Boot Actuator
- [ ] CloudWatch (AWS)
- [ ] 에러 로그 수집

**4. 최종 점검 (1시간)**
- [ ] 전체 기능 테스트
- [ ] 버그 수정

**✅ Day 20 완료 체크리스트**
- [ ] 부하 테스트
- [ ] 성능 지표 수치화
- [ ] 모니터링 설정

---

### Day 21 (2/7, 금) - 최종 마무리
**목표**: 문서화 및 포트폴리오 작성  
**예상 시간**: 6-7시간

#### 오전 (3시간)
**1. 최종 테스트 (2시간)**
- [ ] 전체 기능 수동 테스트
- [ ] Slack 알림 확인 (실제 수신)
- [ ] WebSocket 동기화 확인
- [ ] 모바일 반응형 확인

**2. 버그 수정 (1시간)**

#### 오후 (3-4시간)
**3. README 최종 정리 (2시간)**
```markdown
# Chronicle

## 실사용 증명
- Slack 알림 스크린샷
- 멀티 디바이스 동기화 영상
- 3주간 사용 데이터

## 성능 개선
Before/After 수치

## 기술 블로그 작성 (선택)
- TDD 개발 후기
- WebSocket 실시간 동기화 구현
- Slack API 연동 경험
```

**4. 포트폴리오 작성 (1-2시간)**
```
[Chronicle] 실시간 개인 생산성 관리 앱
...
```

**✅ Day 21 완료 체크리스트**
- [ ] 모든 기능 동작 확인
- [ ] README 완성
- [ ] 포트폴리오 작성
- [ ] 데모 영상 (선택)
- [ ] Git commit (docs: finalize project)

---

## 📋 전체 완료 체크리스트

### Week 1: 도메인 & CRUD
- [ ] Day 1: User, DailyNote Entity
- [ ] Day 2: Task, Attachment, SlackIntegration Entity
- [ ] Day 3: DTO 설계
- [ ] Day 4: DailyNoteService
- [ ] Day 5: TaskService
- [ ] Day 6: Controllers
- [ ] Day 7: UserService & 정리

### Week 2: 인증 & 통합 기능
- [ ] Day 8: JWT 인증 기본
- [ ] Day 9: 인증 완성
- [ ] Day 10: WebSocket
- [ ] Day 11: Slack API
- [ ] Day 12: S3 파일 업로드
- [ ] Day 13: Redis 캐싱
- [ ] Day 14: 리팩토링

### Week 3: 프론트 & 배포
- [ ] Day 15-16: React UI
- [ ] Day 17: WebSocket & 에디터
- [ ] Day 18-19: 배포
- [ ] Day 20: 성능 테스트
- [ ] Day 21: 최종 마무리

---

**화이팅!** 🚀

매일 아침 Slack으로 오늘 할 일 받으면서 개발하는 재미를 느껴보세요! 😊
