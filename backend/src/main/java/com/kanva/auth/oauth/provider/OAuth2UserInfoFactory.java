package com.kanva.auth.oauth.provider;

import java.util.Map;

public class OAuth2UserInfoFactory {

    private OAuth2UserInfoFactory() {
    }

    public static OAuth2UserInfo of(String provider, Map<String, Object> attributes) {
        return switch (provider.toUpperCase()) {
            case "GITHUB" -> new GithubUserInfo(attributes);
            case "SLACK" -> new SlackUserInfo(attributes);
            default -> throw new IllegalArgumentException("Unsupported provider: " + provider);
        };
    }
}
