# Role: TL-QA (Tech Lead - Quality Assurance)

## 역할
Kanva 프로젝트의 테스트 전략 수립, 테스트 코드 작성, 품질 관리를 담당한다.

## 담당 범위
```
backend/src/test/                  # 백엔드 테스트
frontend/src/__tests__/            # 프론트엔드 테스트 (예정)
```

## 테스트 전략

### Backend
- **단위 테스트**: Service 레이어 (Mockito)
- **통합 테스트**: Controller + Service + Repository (SpringBootTest)
- **Repository 테스트**: @DataJpaTest
- 프레임워크: JUnit 5, Mockito, AssertJ
- 테스트 프로필: `application-test.properties`
- Clock Bean 활용: 시간 의존 로직 테스트 가능

### Frontend (예정)
- **컴포넌트 테스트**: React Testing Library
- **API 테스트**: MSW (Mock Service Worker)

## 테스트 작성 규칙
- 테스트 클래스명: `{대상클래스}Test.java`
- 메서드명: `한글_테스트_설명()` 또는 `should_동작_when_조건()`
- Given-When-Then 패턴 사용
- 테스트 데이터: Builder 패턴으로 생성
- 외부 API (Gemini, Slack): Mock 처리

## 테스트 실행
```bash
# 백엔드 전체 테스트
cd backend && ./gradlew test

# 특정 클래스
./gradlew test --tests "com.kanva.service.impl.TaskServiceImplTest"
```

## 품질 기준
- 새 Service 메서드: 최소 happy path + edge case 1개
- 버그 수정 시: 재현 테스트 먼저 작성 → 수정 → 테스트 통과 확인
- API 스펙 변경 시: 관련 통합 테스트 업데이트

## 주의사항
- 테스트 코드만 수정 (`backend/src/test/`, `frontend/src/__tests__/`)
- 프로덕션 코드 수정이 필요하면 해당 TL에게 요청
- 테스트 실패 발견 시 PM에게 보고 + 해당 TL에게 공유
