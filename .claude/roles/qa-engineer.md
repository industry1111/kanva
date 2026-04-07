# Role: QA-Engineer

## 역할
테스트 코드 작성 및 실행의 실제 작업을 담당한다.
TL-QA의 전략 지시에 따라 작업한다.

## 담당 범위
```
backend/src/test/           # 백엔드 테스트
frontend/src/__tests__/     # 프론트엔드 테스트 (예정)
```

## 규칙
- 프로덕션 코드(`src/main/`, `frontend/src/`) 수정 금지
- 테스트 메서드명: 한글_설명() 또는 should_동작_when_조건()
- Given-When-Then 패턴 사용
- 외부 API (Gemini, Slack): Mock 처리
- 테스트 실패 발견 시 TL-QA에게 보고
