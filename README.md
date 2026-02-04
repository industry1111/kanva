<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk&logoColor=white"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-3.5-6DB33F?style=flat-square&logo=springboot&logoColor=white"/>
  <img src="https://img.shields.io/badge/React-19-61DAFB?style=flat-square&logo=react&logoColor=black"/>
  <img src="https://img.shields.io/badge/TypeScript-5.9-3178C6?style=flat-square&logo=typescript&logoColor=white"/>
  <img src="https://img.shields.io/badge/PostgreSQL-17-4169E1?style=flat-square&logo=postgresql&logoColor=white"/>
  <img src="https://img.shields.io/badge/AWS-EC2%20%2B%20RDS-FF9900?style=flat-square&logo=amazonaws&logoColor=white"/>
</p>

<h1 align="center">Kanva</h1>

<p align="center">
  <b>개인 생산성 관리 및 분석 서비스</b><br/>
  Daily Note · Task Management · Productivity Dashboard
</p>

<p align="center">
  🌐 <a href="https://kanva.work"><b>https://kanva.work</b></a>
</p>

---

## Overview

**Kanva**는 일일 노트와 할 일 관리를 통합하여,
개인의 업무 흐름과 생산성을 관리할 수 있도록 만든 서비스입니다.

단순한 CRUD 프로젝트를 넘어,
인증, 스케줄링, 알림, 배포까지 포함된 실제 서비스 환경을 가정하고,
기획부터 배포까지 직접 경험해보기 위해 개발했습니다.

---

## Background

Kanva는 Java와 Spring 기반 백엔드 개발 역량을 다시 정리 및 
기존에 사용하던 기술들을 복습하면서,
실제 서비스에 적용해보는 것을 목표로 진행했습니다.


단순 예제 수준이 아닌,

- OAuth 인증
- 스케줄러 기반 알림
- 대시보드 통계
- AWS 배포 환경

까지 포함한 구조를 직접 설계하고 구현하는 것을 목표로 진행했습니다.

백엔드 전반의 구조 설계와 인프라 구성은 직접 담당했으며,
프론트엔드는 개발 생산성을 높이기 위해 Claude CLI를 적극 활용했습니다.

---

## Key Features

| Feature | Description | Status |
|---------|-------------|--------|
| Daily Note | 날짜별 마크다운 노트 관리 | ✅ |
| Task Management | 할 일 CRUD 및 상태 관리 | ✅ |
| Repeating Tasks | 반복 Task 자동 생성 | ✅ |
| Slack Notifications | 아침/저녁 리마인더 | ✅ |
| Dashboard | 월별/일별 생산성 통계 | ✅ |
| AI Report (Prototype) | 주간 리포트 시범 기능 | ✅ |
| OAuth Login | GitHub / Slack 로그인 | ✅ |

---

## Frontend Development Note

프론트엔드(React)는 개인 프로젝트 특성상
UI 구조 설계와 반복 작업의 효율을 높이기 위해
Claude CLI를 활용해 기본 UI 구조를 생성한 뒤,
백엔드 API 구조에 맞게 직접 수정·보완하며 연동했습니다.

---

## AI Integration (In Progress)

AI 기능은 현재 실험 단계로,
우선 기본적인 생산성 분석과 리포트 기능부터 구현하고 있습니다.

현재는 Task 데이터를 기반으로 한
Rule-based 방식으로 완료율과 트렌드를 계산하고 있으며,
실제로 사용하면서 부족한 부분을 계속 보완하고 있습니다.

초기에는 Ollama 기반 로컬 LLM 연동도 고려했지만,
데이터 관리와 운영 안정성을 우선으로 생각해
현재는 Gemini API 기반 구조로 방향을 잡았습니다.

향후 Google Gemini 2.5 Flash API를 활용하여
자연어 기반 분석과 회고 요약 기능을 단계적으로 추가할 예정이며,
우선은 현재 구조를 안정화하는 데 집중하고 있습니다.

---

### AI Weekly Report (Prototype)

주간 단위로 작업 현황을 정리해주는 리포트 기능입니다.

- 완료율 계산
- 이전 기간 대비 트렌드 분석
- 간단한 인사이트 및 추천 제공
- 사용자 피드백 수집

현재는 통계 기반 프로토타입 형태로 운영 중입니다.

---

## Architecture

Frontend (React + TypeScript)
↓
REST API (JWT)
↓
Backend (Spring Boot)
↓
PostgreSQL / Slack / GitHub OAuth

백엔드 중심으로 구조를 설계했으며,
기능이 늘어날 것을 고려해,
초기 설계 단계에서 계층을 분리해 구성했습니다.

---

## Tech Stack

### Backend
- Java 21
- Spring Boot 3.5
- Spring Security + JWT
- JPA / Hibernate
- PostgreSQL

### Frontend
- React 19
- TypeScript
- Vite

### Infrastructure
- AWS EC2
- AWS RDS
- Nginx
- Let's Encrypt

---

## Key Implementations

### 1. Repeating Task System

반복되는 할 일을 효율적으로 관리하기 위해
TaskSeries 기반 구조를 설계했습니다.

- 조회 시점에 자동 생성 (On-demand)
- 불필요한 미래 데이터 생성 방지
- 완료 정책 분리

---

### 2. Dashboard Query Optimization

월별 통계 조회 시 N+1 문제를 방지하기 위해
단일 쿼리 기반으로 조회하도록 구조를 수정했습니다.

---

### 3. OAuth Multi-Provider Connection

GitHub과 Slack 계정을 동시에 연결할 수 있도록 설계했습니다.

Provider별 특성을 분리하여 관리하고,
Slack 연동 정보는 알림 기능에 활용합니다.

---

## API Endpoints (Main)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/auth/oauth/{provider}/callback | OAuth 로그인 |
| GET | /api/tasks | Task 조회 |
| POST | /api/tasks | Task 생성 |
| PATCH | /api/tasks/{id}/toggle | 완료 토글 |
| GET | /api/dashboard | 대시보드 조회 |
| GET | /api/reports | AI 리포트 조회 |
| POST | /api/reports | 리포트 생성 |

---

## Project Structure

```
kanva/
├── backend/src/main/java/com/kanva/
│   ├── auth/oauth/provider/    # OAuth Provider Adapter 패턴
│   ├── common/                 # ApiResponse, ErrorCode
│   ├── config/                 # Security, Clock, JPA Config
│   ├── controller/             # REST Controllers
│   │   └── report/             # AI Report Controller
│   ├── domain/                 # Entities + Repositories
│   │   └── report/             # AIReport, ReportStatus, ReportFeedback
│   ├── dto/                    # Request/Response DTOs
│   ├── exception/              # Global Exception Handler
│   ├── scheduler/              # TaskSeries, Notification Schedulers
│   ├── security/               # JWT Provider, Filter
│   └── service/
│       ├── impl/               # Service 구현체
│       └── report/             # AIAnalysisService (확장 가능 구조)
│
└── frontend/src/
    ├── components/
    │   ├── dashboard/          # TaskStats, ProductivityChart, AIReportCard
    │   └── report/             # InsightCard, RecommendationCard, FeedbackButton
    ├── contexts/               # AuthContext (OAuth 상태 관리)
    ├── pages/                  # Workspace, Dashboard, AIReportPage
    ├── services/               # API Client (fetchWithAuth)
    └── types/                  # TypeScript 타입 정의
```

