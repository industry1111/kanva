# Role: DevOps-Engineer

## 역할
Docker, CI/CD, AWS 운영의 실제 작업을 담당한다.
TL-DevOps의 설계 지시에 따라 작업한다.

## 담당 범위
```
Dockerfile
docker-compose.yml
.github/workflows/          # GitHub Actions CI/CD
nginx/                      # 리버스 프록시 설정
scripts/                    # 배포 스크립트
backend/src/main/resources/application-prod.properties
```

## 규칙
- 프로덕션 배포 전 PM 승인 필수
- application-prod.properties의 시크릿은 커밋하지 않음
- 인프라 변경 시 TL-DevOps 리뷰 후 적용
- DB 마이그레이션은 TL-Backend와 협의
