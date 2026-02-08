package com.kanva.service.impl;

import com.kanva.domain.user.OAuthProvider;
import com.kanva.domain.user.Role;
import com.kanva.domain.user.User;
import com.kanva.domain.user.UserOAuthConnectionRepository;
import com.kanva.domain.user.UserRepository;
import com.kanva.dto.user.LoginRequest;
import com.kanva.dto.user.LoginResponse;
import com.kanva.dto.user.SignUpRequest;
import com.kanva.dto.user.UserResponse;
import com.kanva.exception.DuplicateNameException;
import com.kanva.exception.InvalidPasswordException;
import com.kanva.exception.UserNotFoundException;
import com.kanva.security.jwt.JwtToken;
import com.kanva.security.jwt.JwtTokenProvider;
import com.kanva.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserOAuthConnectionRepository oauthConnectionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public LoginResponse signUp(SignUpRequest request) {
        if (userRepository.existsByName(request.getName())) {
            throw new DuplicateNameException(request.getName());
        }

        User user = User.builder()
                .name(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        User savedUser = userRepository.save(user);

        // 가입 후 자동 로그인 (JWT 발급)
        String authorities = "ROLE_" + savedUser.getRole().name();
        JwtToken jwtToken = jwtTokenProvider.generateToken(savedUser.getId(), savedUser.getName(), authorities);

        return LoginResponse.of(jwtToken, UserResponse.from(savedUser, Collections.emptyList()));
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByName(request.getName())
                .orElseThrow(() -> new UserNotFoundException(request.getName()));

        if (user.getPassword() == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidPasswordException();
        }

        String authorities = "ROLE_" + user.getRole().name();
        JwtToken jwtToken = jwtTokenProvider.generateToken(user.getId(), user.getName(), authorities);

        List<OAuthProvider> connectedProviders = oauthConnectionRepository.findProvidersByUserId(user.getId());
        return LoginResponse.of(jwtToken, UserResponse.from(user, connectedProviders));
    }

    @Override
    public UserResponse getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        List<OAuthProvider> connectedProviders = oauthConnectionRepository.findProvidersByUserId(userId);
        return UserResponse.from(user, connectedProviders);
    }

    @Override
    public UserResponse getCurrentUser(String identifier) {
        // identifier는 JWT subject - name 또는 email일 수 있음 (OAuth 사용자는 email, 닉네임 사용자는 name)
        User user = userRepository.findByName(identifier)
                .or(() -> userRepository.findByEmail(identifier))
                .orElseThrow(() -> new UserNotFoundException(identifier));
        List<OAuthProvider> connectedProviders = oauthConnectionRepository.findProvidersByUserId(user.getId());
        return UserResponse.from(user, connectedProviders);
    }
}
