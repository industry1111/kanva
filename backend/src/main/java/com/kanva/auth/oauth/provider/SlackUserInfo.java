package com.kanva.auth.oauth.provider;

import java.util.Map;

public class SlackUserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    public SlackUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProvider() {
        return "SLACK";
    }

    @Override
    public String getProviderUserId() {
        Map<String, Object> authedUser = getNestedMap("authed_user");
        return authedUser != null ? (String) authedUser.get("id") : null;
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getName() {
        Map<String, Object> authedUser = getNestedMap("authed_user");
        if (authedUser != null) {
            String name = (String) authedUser.get("name");
            if (name != null) {
                return name;
            }
        }
        return null;
    }

    @Override
    public String getPicture() {
        return (String) attributes.get("picture");
    }

    public String getTeamId() {
        Map<String, Object> team = getNestedMap("team");
        return team != null ? (String) team.get("id") : null;
    }

    public String getTeamName() {
        Map<String, Object> team = getNestedMap("team");
        return team != null ? (String) team.get("name") : null;
    }

    public String getBotAccessToken() {
        return (String) attributes.get("access_token");
    }

    public String getBotUserId() {
        return (String) attributes.get("bot_user_id");
    }

    public String getScope() {
        return (String) attributes.get("scope");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getNestedMap(String key) {
        Object value = attributes.get(key);
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        return null;
    }
}
