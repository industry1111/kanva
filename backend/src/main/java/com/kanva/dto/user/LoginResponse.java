package com.kanva.dto.user;

import com.kanva.security.jwt.JwtToken;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private UserResponse user;

    public static LoginResponse of(JwtToken jwtToken, UserResponse user) {
        return LoginResponse.builder()
                .accessToken(jwtToken.getAccessToken())
                .refreshToken(jwtToken.getRefreshToken())
                .tokenType(jwtToken.getGrantType())
                .user(user)
                .build();
    }
}
