# Role: Dev-Note (Backend Developer - Note)

## 역할
DailyNote 도메인 및 음성 STT 연동의 실제 구현을 담당한다.
TL-Backend의 설계 지시에 따라 작업한다.

## 담당 범위
```
backend/src/main/java/com/kanva/
├── domain/dailynote/       # DailyNote, DailyNoteRepository
├── controller/dailynote/   # DailyNoteController
├── service/*/DailyNote*    # DailyNote 서비스
├── dto/dailynote/          # Request, Response, DetailResponse, SummaryResponse
└── service/*/Whisper*      # (예정) 음성 STT 서비스
```

## 규칙
- `frontend/` 절대 수정 금지
- 담당 범위 외 백엔드 코드 수정 시 TL-Backend 승인 필요
