# API 테스트 (curl 예제)

서버 실행 후 아래 curl 명령어로 API를 테스트할 수 있습니다.

## 서버 실행

```bash
cd backend
./gradlew bootRun
```

서버가 `http://localhost:8080`에서 실행됩니다.

---

## DailyNote API

### 1. 특정 날짜 노트 조회 (없으면 생성)

```bash
curl -X GET "http://localhost:8080/api/daily-notes/2025-01-21" \
  -H "Content-Type: application/json"
```

**예상 응답:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "date": "2025-01-21",
    "content": null,
    "createdAt": "2025-01-21T17:00:00",
    "updatedAt": "2025-01-21T17:00:00"
  },
  "code": 200,
  "message": "조회 성공",
  "errors": null
}
```

### 2. 노트 수정

```bash
curl -X PUT "http://localhost:8080/api/daily-notes/2025-01-21" \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2025-01-21",
    "content": "# 오늘의 할 일\n\n- [ ] API 개발\n- [ ] 테스트 작성"
  }'
```

**예상 응답:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "date": "2025-01-21",
    "content": "# 오늘의 할 일\n\n- [ ] API 개발\n- [ ] 테스트 작성",
    "createdAt": "2025-01-21T17:00:00",
    "updatedAt": "2025-01-21T17:05:00"
  },
  "code": 200,
  "message": "조회 성공",
  "errors": null
}
```

### 3. 노트 삭제

```bash
curl -X DELETE "http://localhost:8080/api/daily-notes/2025-01-21" \
  -H "Content-Type: application/json"
```

**예상 응답:**
```json
{
  "success": true,
  "data": null,
  "code": 200,
  "message": "조회 성공",
  "errors": null
}
```

### 4. 월별 노트 목록 조회

```bash
curl -X GET "http://localhost:8080/api/daily-notes/calendar?month=2025-01" \
  -H "Content-Type: application/json"
```

**예상 응답:**
```json
{
  "success": true,
  "data": [
    {
      "date": "2025-01-20",
      "hasContent": true
    },
    {
      "date": "2025-01-21",
      "hasContent": false
    }
  ],
  "code": 200,
  "message": "조회 성공",
  "errors": null
}
```

---

## Task API

### 1. 특정 날짜의 Task 목록 조회

```bash
curl -X GET "http://localhost:8080/api/tasks?date=2025-01-21" \
  -H "Content-Type: application/json"
```

**예상 응답:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "dailyNoteId": 1,
      "title": "API 개발",
      "description": null,
      "dueDate": null,
      "status": "PENDING",
      "position": 0,
      "overdue": false,
      "createdAt": "2025-01-21T17:00:00",
      "updatedAt": "2025-01-21T17:00:00"
    }
  ],
  "code": 200,
  "message": "조회 성공",
  "errors": null
}
```

### 2. Task 생성

```bash
curl -X POST "http://localhost:8080/api/tasks?date=2025-01-21" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "새로운 할 일",
    "description": "할 일 상세 설명",
    "status": "PENDING"
  }'
```

**예상 응답:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "dailyNoteId": 1,
    "title": "새로운 할 일",
    "description": "할 일 상세 설명",
    "dueDate": null,
    "status": "PENDING",
    "position": 0,
    "overdue": false,
    "createdAt": "2025-01-21T17:00:00",
    "updatedAt": "2025-01-21T17:00:00"
  },
  "code": 201,
  "message": "생성 성공",
  "errors": null
}
```

### 3. Task 완료 상태 토글

```bash
curl -X PATCH "http://localhost:8080/api/tasks/1/toggle" \
  -H "Content-Type: application/json"
```

**예상 응답:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "dailyNoteId": 1,
    "title": "새로운 할 일",
    "description": "할 일 상세 설명",
    "dueDate": null,
    "status": "COMPLETED",
    "position": 0,
    "overdue": false,
    "createdAt": "2025-01-21T17:00:00",
    "updatedAt": "2025-01-21T17:10:00"
  },
  "code": 200,
  "message": "조회 성공",
  "errors": null
}
```

### 4. Task 삭제

```bash
curl -X DELETE "http://localhost:8080/api/tasks/1" \
  -H "Content-Type: application/json"
```

**예상 응답:**
```json
{
  "success": true,
  "data": null,
  "code": 200,
  "message": "조회 성공",
  "errors": null
}
```

### 5. Task 단건 조회

```bash
curl -X GET "http://localhost:8080/api/tasks/1" \
  -H "Content-Type: application/json"
```

### 6. Task 수정

```bash
curl -X PUT "http://localhost:8080/api/tasks/1" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "수정된 할 일",
    "description": "수정된 설명",
    "status": "IN_PROGRESS"
  }'
```

### 7. Task 상태 변경

```bash
curl -X PATCH "http://localhost:8080/api/tasks/1/status" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "COMPLETED"
  }'
```

### 8. 마감 지난 Task 목록 조회

```bash
curl -X GET "http://localhost:8080/api/tasks/overdue" \
  -H "Content-Type: application/json"
```

---

## 에러 응답 예시

### 404 Not Found (리소스 없음)

```json
{
  "success": false,
  "data": null,
  "code": 404,
  "message": "할 일을 찾을 수 없습니다.",
  "errors": null
}
```

### 400 Bad Request (잘못된 요청)

```json
{
  "success": false,
  "data": null,
  "code": 400,
  "message": "입력값이 올바르지 않습니다.",
  "errors": [
    {
      "field": "title",
      "message": "제목은 필수입니다."
    }
  ]
}
```

---

## CORS 설정

현재 개발 환경에서는 다음 설정이 적용되어 있습니다:

- **Allowed Origins**: `http://localhost:5173`, `http://localhost:3000`
- **Allowed Methods**: GET, POST, PUT, PATCH, DELETE, OPTIONS
- **Allowed Headers**: *
- **Credentials**: false (임시 개발용)

---

## 임시 개발용 설정

현재 `/api/**` 경로는 인증 없이 접근 가능합니다 (permitAll).
모든 API 요청에서 `userId = 1L`을 사용합니다.

**프로덕션 배포 전 반드시 인증 로직을 복구해야 합니다!**
