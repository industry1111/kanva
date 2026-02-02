package com.kanva.domain.slack;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SlackConnectionRepository extends JpaRepository<SlackConnection, Long> {

    Optional<SlackConnection> findByUserId(Long userId);

    Optional<SlackConnection> findBySlackUserId(String slackUserId);

    List<SlackConnection> findAllByNotificationsEnabledTrue();

    boolean existsByUserId(Long userId);
}
