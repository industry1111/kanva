package com.kanva.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserOAuthConnectionRepository extends JpaRepository<UserOAuthConnection, Long> {

    Optional<UserOAuthConnection> findByProviderAndProviderId(OAuthProvider provider, String providerId);

    List<UserOAuthConnection> findByUserId(Long userId);

    boolean existsByUserIdAndProvider(Long userId, OAuthProvider provider);

    @Query("SELECT c.provider FROM UserOAuthConnection c WHERE c.user.id = :userId")
    List<OAuthProvider> findProvidersByUserId(@Param("userId") Long userId);
}
