package com.kanva.domain.user;

import com.kanva.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_oauth_connections",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"provider", "provider_id"}),
           @UniqueConstraint(columnNames = {"user_id", "provider"})
       })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserOAuthConnection extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OAuthProvider provider;

    @Column(name = "provider_id", nullable = false, length = 100)
    private String providerId;

    @Column(length = 500)
    private String picture;

    @Builder
    public UserOAuthConnection(User user, OAuthProvider provider, String providerId, String picture) {
        this.user = user;
        this.provider = provider;
        this.providerId = providerId;
        this.picture = picture;
    }

    public void updatePicture(String picture) {
        this.picture = picture;
    }
}
