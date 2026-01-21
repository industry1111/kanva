package com.kanva.service;

import com.kanva.dto.user.LoginRequest;
import com.kanva.dto.user.LoginResponse;
import com.kanva.dto.user.SignUpRequest;
import com.kanva.dto.user.UserResponse;

public interface UserService {

    /**
     * 회원가입
     */
    UserResponse signUp(SignUpRequest request);

    /**
     * 로그인
     */
    LoginResponse login(LoginRequest request);

    /**
     * 사용자 정보 조회
     */
    UserResponse getUser(Long userId);

    /**
     * 현재 로그인한 사용자 정보 조회
     */
    UserResponse getCurrentUser(String email);
}
