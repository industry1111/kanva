package com.kanva.dto.user;

import com.kanva.domain.user.OAuthProvider;
import com.kanva.domain.user.Role;
import com.kanva.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private String name;
    private Role role;
    private String picture;
    private List<OAuthProvider> connectedProviders;
    private LocalDateTime createdAt;

    public static UserResponse from(User user, List<OAuthProvider> connectedProviders) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .picture(user.getPicture())
                .connectedProviders(connectedProviders)
                .createdAt(user.getCreatedAt())
                .build();
    }
}
