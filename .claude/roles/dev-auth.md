# Role: Dev-Auth (Backend Developer - Auth)

## 역할
인증/인가, OAuth, Security 도메인의 실제 구현을 담당한다.
TL-Backend의 설계 지시에 따라 작업한다.

## 담당 범위
```
backend/src/main/java/com/kanva/
├── auth/oauth/provider/    # OAuth2UserInfo, GithubUserInfo, SlackUserInfo
├── config/SecurityConfig   # Spring Security 설정
├── config/OAuthConfig      # OAuth 설정
├── security/               # JWT, UserPrincipal, CustomUserDetails
├── controller/auth/        # AuthController
├── service/*/OAuth*        # OAuth 서비스
├── service/*/User*         # User 서비스
├── domain/user/            # User, Role, UserOAuthConnection, OAuthProvider
├── dto/auth/               # OAuthCallbackRequest, OAuthLoginUrlResponse
└── dto/user/               # LoginRequest/Response, SignUpRequest, UserResponse
```

## 규칙
- `frontend/` 절대 수정 금지
- 담당 범위 외 백엔드 코드 수정 시 TL-Backend 승인 필요
- 보안 관련 변경은 반드시 TL-Backend 리뷰 후 적용
