# Role: Dev-Core (Frontend Developer - Core)

## 역할
AuthContext, API 래퍼, 공통 컴포넌트의 실제 구현을 담당한다.
TL-Frontend의 설계 지시에 따라 작업한다.

## 담당 범위
```
frontend/src/
├── contexts/               # AuthContext
├── services/               # api.ts (fetchWithAuth, 각 도메인 API 함수)
├── types/                  # api.ts (TypeScript 인터페이스)
├── components/common/      # Modal 등 공통 컴포넌트
├── pages/LoginPage.tsx
└── pages/OAuthCallbackPage.tsx
```

## 규칙
- `backend/` 절대 수정 금지
- 스타일링: Tailwind CSS 클래스만 사용 (inline style 금지)
- 타입 변경 시 Dev-Workspace, Dev-Dashboard에 영향 공유
- API 함수 시그니처 변경 시 TL-Frontend 승인 필요
