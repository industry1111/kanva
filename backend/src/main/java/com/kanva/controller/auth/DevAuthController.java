package com.kanva.controller.auth;

import com.kanva.common.response.ApiResponse;
import com.kanva.domain.user.Role;
import com.kanva.domain.user.User;
import com.kanva.domain.user.UserRepository;
import com.kanva.dto.user.LoginResponse;
import com.kanva.dto.user.UserResponse;
import com.kanva.security.jwt.JwtToken;
import com.kanva.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 로컬 개발 전용 자동 로그인
 * local 프로필에서만 활성화
 */
@Profile("local")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class DevAuthController {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/dev-login")
    public ResponseEntity<ApiResponse<LoginResponse>> devLogin() {
        User user = userRepository.findByEmail("dev@kanva.local")
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .email("dev@kanva.local")
                                .name("DEV")
                                .password("")
                                .role(Role.USER)
                                .build()
                ));

        JwtToken token = jwtTokenProvider.generateToken(
                user.getId(), user.getEmail(), "ROLE_USER"
        );

        UserResponse userResponse = UserResponse.from(user, List.of());
        LoginResponse loginResponse = LoginResponse.of(token, userResponse);

        return ResponseEntity.ok(ApiResponse.ok(loginResponse));
    }
}
