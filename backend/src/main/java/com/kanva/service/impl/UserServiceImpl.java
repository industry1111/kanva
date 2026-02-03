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
import com.kanva.exception.DuplicateEmailException;
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
    public UserResponse signUp(SignUpRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(request.getEmail());
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .role(Role.USER)
                .build();

        User savedUser = userRepository.save(user);
        return UserResponse.from(savedUser, Collections.emptyList());
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException(request.getEmail()));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidPasswordException();
        }

        String authorities = "ROLE_" + user.getRole().name();
        JwtToken jwtToken = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), authorities);

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
    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
        List<OAuthProvider> connectedProviders = oauthConnectionRepository.findProvidersByUserId(user.getId());
        return UserResponse.from(user, connectedProviders);
    }
}
