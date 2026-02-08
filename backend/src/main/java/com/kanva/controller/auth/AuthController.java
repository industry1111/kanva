package com.kanva.controller.auth;

import com.kanva.common.response.ApiResponse;
import com.kanva.domain.user.OAuthProvider;
import com.kanva.dto.auth.OAuthCallbackRequest;
import com.kanva.dto.auth.OAuthLoginUrlResponse;
import com.kanva.dto.user.LoginRequest;
import com.kanva.dto.user.LoginResponse;
import com.kanva.dto.user.SignUpRequest;
import com.kanva.dto.user.UserResponse;
import com.kanva.service.OAuthService;
import com.kanva.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final OAuthService oAuthService;

    /**
     * 회원가입 (가입 후 자동 로그인)
     * POST /api/auth/signup
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<LoginResponse>> signUp(@Valid @RequestBody SignUpRequest request) {
        LoginResponse response = userService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response));
    }

    /**
     * 로그인
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * 현재 로그인한 사용자 정보 조회
     * GET /api/auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails) {
        UserResponse response = userService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * OAuth 로그인 URL 조회
     * GET /api/auth/oauth/{provider}/login-url
     */
    @GetMapping("/oauth/{provider}/login-url")
    public ResponseEntity<ApiResponse<OAuthLoginUrlResponse>> getOAuthLoginUrl(
            @PathVariable String provider) {
        OAuthProvider oauthProvider = OAuthProvider.valueOf(provider.toUpperCase());
        OAuthLoginUrlResponse response = oAuthService.getLoginUrl(oauthProvider);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * OAuth 콜백 처리
     * POST /api/auth/oauth/{provider}/callback
     */
    @PostMapping("/oauth/{provider}/callback")
    public ResponseEntity<ApiResponse<LoginResponse>> oauthCallback(
            @PathVariable String provider,
            @Valid @RequestBody OAuthCallbackRequest request) {
        OAuthProvider oauthProvider = OAuthProvider.valueOf(provider.toUpperCase());
        LoginResponse response = oAuthService.processCallback(oauthProvider, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
