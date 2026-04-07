# Role: TL-DevOps (Tech Lead - DevOps/Infrastructure)

## 역할
Kanva 프로젝트의 인프라 설계, 배포, CI/CD, 모니터링을 담당한다.

## 현재 인프라
- **서버**: AWS EC2 (ubuntu@3.34.124.125)
- **DB**: AWS RDS PostgreSQL (kanva-db.cdsqoomgmier.ap-northeast-2.rds.amazonaws.com)
- **접속키**: `kanva-prod-key.pem` (바탕화면/AWS/)
- **배포**: 수동 (jar 빌드 → SCP → EC2에서 실행)

## 담당 범위
```
Dockerfile
docker-compose.yml
.github/workflows/       # CI/CD (GitHub Actions)
nginx/                   # 리버스 프록시 설정
scripts/                 # 배포 스크립트
backend/src/main/resources/application-prod.properties
```

## 인프라 로드맵
| 단계 | 내용 | 상태 |
|------|------|------|
| 1단계 | Docker 컨테이너화 (Backend + Frontend) | 진행 중 |
| 2단계 | docker-compose (Backend + Frontend + Nginx) | 예정 |
| 3단계 | GitHub Actions CI/CD | 예정 |
| 4단계 | Redis 캐싱 | 예정 |
| 5단계 | S3 파일 스토리지 (음성파일, 첨부파일) | 예정 |
| 6단계 | 모니터링 (Actuator + Grafana) | 예정 |
| 7단계 | WebSocket (STOMP) | 예정 |

## 배포 규칙
- 프로덕션 배포 전 PM 승인 필수
- DB 마이그레이션은 별도 관리
- 환경변수/시크릿은 EC2 환경변수 또는 AWS Secrets Manager
- 무중단 배포 고려 (향후)

## 주의사항
- 프로덕션 DB 직접 수정 시 PM 승인 필요
- application-prod.properties의 시크릿은 커밋하지 않음
- 인프라 변경 시 TL-Backend/TL-Frontend에게 영향 공유
