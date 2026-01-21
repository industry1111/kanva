package com.kanva.service;

import com.kanva.domain.user.Role;
import com.kanva.domain.user.User;
import com.kanva.domain.user.UserRepository;
import com.kanva.dto.user.LoginRequest;
import com.kanva.dto.user.LoginResponse;
import com.kanva.dto.user.SignUpRequest;
import com.kanva.dto.user.UserResponse;
import com.kanva.exception.DuplicateEmailException;
import com.kanva.exception.InvalidPasswordException;
import com.kanva.exception.UserNotFoundException;
import com.kanva.security.jwt.JwtToken;
import com.kanva.security.jwt.JwtTokenProvider;
import com.kanva.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 테스트")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = createUser(1L, "test@example.com", "테스트유저", "encodedPassword");
    }

    private User createUser(Long id, String email, String name, String password) {
        User user = User.builder()
                .email(email)
                .password(password)
                .name(name)
                .role(Role.USER)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        ReflectionTestUtils.setField(user, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(user, "updatedAt", LocalDateTime.now());
        return user;
    }

    @Nested
    @DisplayName("signUp 메서드")
    class SignUp {

        @Test
        @DisplayName("회원가입에 성공한다")
        void success() {
            // given
            SignUpRequest request = SignUpRequest.builder()
                    .email("new@example.com")
                    .password("password123")
                    .name("새로운유저")
                    .build();

            given(userRepository.existsByEmail("new@example.com")).willReturn(false);
            given(passwordEncoder.encode("password123")).willReturn("encodedPassword");
            given(userRepository.save(any(User.class))).willAnswer(invocation -> {
                User savedUser = invocation.getArgument(0);
                ReflectionTestUtils.setField(savedUser, "id", 1L);
                ReflectionTestUtils.setField(savedUser, "createdAt", LocalDateTime.now());
                ReflectionTestUtils.setField(savedUser, "updatedAt", LocalDateTime.now());
                return savedUser;
            });

            // when
            UserResponse response = userService.signUp(request);

            // then
            assertThat(response.getEmail()).isEqualTo("new@example.com");
            assertThat(response.getName()).isEqualTo("새로운유저");
            assertThat(response.getRole()).isEqualTo(Role.USER);
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("이미 존재하는 이메일로 가입 시 예외 발생")
        void duplicateEmail() {
            // given
            SignUpRequest request = SignUpRequest.builder()
                    .email("test@example.com")
                    .password("password123")
                    .name("테스트유저")
                    .build();

            given(userRepository.existsByEmail("test@example.com")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> userService.signUp(request))
                    .isInstanceOf(DuplicateEmailException.class);
        }
    }

    @Nested
    @DisplayName("login 메서드")
    class Login {

        @Test
        @DisplayName("로그인에 성공한다")
        void success() {
            // given
            LoginRequest request = LoginRequest.builder()
                    .email("test@example.com")
                    .password("password123")
                    .build();

            JwtToken jwtToken = JwtToken.builder()
                    .grantType("Bearer")
                    .accessToken("access-token")
                    .refreshToken("refresh-token")
                    .build();

            given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(user));
            given(passwordEncoder.matches("password123", "encodedPassword")).willReturn(true);
            given(jwtTokenProvider.generateToken(anyString(), anyString())).willReturn(jwtToken);

            // when
            LoginResponse response = userService.login(request);

            // then
            assertThat(response.getAccessToken()).isEqualTo("access-token");
            assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
            assertThat(response.getTokenType()).isEqualTo("Bearer");
            assertThat(response.getUser().getEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 로그인 시 예외 발생")
        void userNotFound() {
            // given
            LoginRequest request = LoginRequest.builder()
                    .email("notfound@example.com")
                    .password("password123")
                    .build();

            given(userRepository.findByEmail("notfound@example.com")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.login(request))
                    .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        @DisplayName("비밀번호가 일치하지 않으면 예외 발생")
        void invalidPassword() {
            // given
            LoginRequest request = LoginRequest.builder()
                    .email("test@example.com")
                    .password("wrongPassword")
                    .build();

            given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(user));
            given(passwordEncoder.matches("wrongPassword", "encodedPassword")).willReturn(false);

            // when & then
            assertThatThrownBy(() -> userService.login(request))
                    .isInstanceOf(InvalidPasswordException.class);
        }
    }

    @Nested
    @DisplayName("getUser 메서드")
    class GetUser {

        @Test
        @DisplayName("사용자를 조회한다")
        void success() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            // when
            UserResponse response = userService.getUser(1L);

            // then
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("존재하지 않는 사용자 조회 시 예외 발생")
        void notFound() {
            // given
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.getUser(999L))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getCurrentUser 메서드")
    class GetCurrentUser {

        @Test
        @DisplayName("이메일로 현재 사용자를 조회한다")
        void success() {
            // given
            given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(user));

            // when
            UserResponse response = userService.getCurrentUser("test@example.com");

            // then
            assertThat(response.getEmail()).isEqualTo("test@example.com");
        }
    }
}
