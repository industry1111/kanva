package com.kanva.controller.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanva.domain.user.Role;
import com.kanva.dto.user.LoginRequest;
import com.kanva.dto.user.LoginResponse;
import com.kanva.dto.user.SignUpRequest;
import com.kanva.dto.user.UserResponse;
import com.kanva.exception.DuplicateEmailException;
import com.kanva.exception.GlobalExceptionHandler;
import com.kanva.exception.InvalidPasswordException;
import com.kanva.exception.UserNotFoundException;
import com.kanva.security.CustomUserDetailsService;
import com.kanva.security.jwt.JwtAuthenticationFilter;
import com.kanva.security.jwt.JwtTokenProvider;
import com.kanva.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = AuthController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@DisplayName("AuthController 테스트")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Nested
    @DisplayName("POST /api/auth/signup")
    class SignUp {

        @Test
        @DisplayName("회원가입에 성공한다")
        void success() throws Exception {
            // given
            SignUpRequest request = SignUpRequest.builder()
                    .email("test@example.com")
                    .password("password123")
                    .name("테스트유저")
                    .build();

            UserResponse response = UserResponse.builder()
                    .id(1L)
                    .email("test@example.com")
                    .name("테스트유저")
                    .role(Role.USER)
                    .createdAt(LocalDateTime.now())
                    .build();

            given(userService.signUp(any(SignUpRequest.class))).willReturn(response);

            // when & then
            mockMvc.perform(post("/api/auth/signup")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.email").value("test@example.com"))
                    .andExpect(jsonPath("$.data.name").value("테스트유저"))
                    .andExpect(jsonPath("$.code").value(201));
        }

        @Test
        @DisplayName("이미 존재하는 이메일로 가입 시 409 에러를 반환한다")
        void duplicateEmail() throws Exception {
            // given
            SignUpRequest request = SignUpRequest.builder()
                    .email("test@example.com")
                    .password("password123")
                    .name("테스트유저")
                    .build();

            given(userService.signUp(any(SignUpRequest.class)))
                    .willThrow(new DuplicateEmailException("test@example.com"));

            // when & then
            mockMvc.perform(post("/api/auth/signup")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("이메일 형식이 올바르지 않으면 400 에러를 반환한다")
        void invalidEmailFormat() throws Exception {
            // given
            SignUpRequest request = SignUpRequest.builder()
                    .email("invalid-email")
                    .password("password123")
                    .name("테스트유저")
                    .build();

            // when & then
            mockMvc.perform(post("/api/auth/signup")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("비밀번호가 8자 미만이면 400 에러를 반환한다")
        void shortPassword() throws Exception {
            // given
            SignUpRequest request = SignUpRequest.builder()
                    .email("test@example.com")
                    .password("short")
                    .name("테스트유저")
                    .build();

            // when & then
            mockMvc.perform(post("/api/auth/signup")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/login")
    class Login {

        @Test
        @DisplayName("로그인에 성공한다")
        void success() throws Exception {
            // given
            LoginRequest request = LoginRequest.builder()
                    .email("test@example.com")
                    .password("password123")
                    .build();

            UserResponse userResponse = UserResponse.builder()
                    .id(1L)
                    .email("test@example.com")
                    .name("테스트유저")
                    .role(Role.USER)
                    .createdAt(LocalDateTime.now())
                    .build();

            LoginResponse response = LoginResponse.builder()
                    .accessToken("access-token")
                    .refreshToken("refresh-token")
                    .tokenType("Bearer")
                    .user(userResponse)
                    .build();

            given(userService.login(any(LoginRequest.class))).willReturn(response);

            // when & then
            mockMvc.perform(post("/api/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                    .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"))
                    .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                    .andExpect(jsonPath("$.data.user.email").value("test@example.com"));
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 로그인 시 404 에러를 반환한다")
        void userNotFound() throws Exception {
            // given
            LoginRequest request = LoginRequest.builder()
                    .email("notfound@example.com")
                    .password("password123")
                    .build();

            given(userService.login(any(LoginRequest.class)))
                    .willThrow(new UserNotFoundException("notfound@example.com"));

            // when & then
            mockMvc.perform(post("/api/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("비밀번호가 일치하지 않으면 401 에러를 반환한다")
        void invalidPassword() throws Exception {
            // given
            LoginRequest request = LoginRequest.builder()
                    .email("test@example.com")
                    .password("wrongPassword")
                    .build();

            given(userService.login(any(LoginRequest.class)))
                    .willThrow(new InvalidPasswordException());

            // when & then
            mockMvc.perform(post("/api/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/auth/me")
    class GetCurrentUser {

        @Test
        @WithMockUser(username = "test@example.com")
        @DisplayName("현재 로그인한 사용자 정보를 조회한다")
        void success() throws Exception {
            // given
            UserResponse response = UserResponse.builder()
                    .id(1L)
                    .email("test@example.com")
                    .name("테스트유저")
                    .role(Role.USER)
                    .createdAt(LocalDateTime.now())
                    .build();

            given(userService.getCurrentUser("test@example.com")).willReturn(response);

            // when & then
            mockMvc.perform(get("/api/auth/me"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.email").value("test@example.com"));
        }
    }
}
