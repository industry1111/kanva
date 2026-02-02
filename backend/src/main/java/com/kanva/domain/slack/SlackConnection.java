package com.kanva.domain.slack;

import com.kanva.domain.BaseEntity;
import com.kanva.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "slack_connections")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SlackConnection extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String slackUserId;

    @Column(nullable = false)
    private String teamId;

    private String teamName;

    @Column(length = 500)
    private String botToken;

    @Builder.Default
    @Column(nullable = false)
    private boolean notificationsEnabled = true;

    public void updateBotToken(String botToken) {
        this.botToken = botToken;
    }

    public void enableNotifications() {
        this.notificationsEnabled = true;
    }

    public void disableNotifications() {
        this.notificationsEnabled = false;
    }
}
