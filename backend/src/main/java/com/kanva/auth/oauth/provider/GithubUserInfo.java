package com.kanva.auth.oauth.provider;

import java.util.Map;

public class GithubUserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    public GithubUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProvider() {
        return "GITHUB";
    }

    @Override
    public String getProviderUserId() {
        Object id = attributes.get("id");
        return id != null ? String.valueOf(id) : null;
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getName() {
        String name = (String) attributes.get("name");
        if (name == null) {
            name = (String) attributes.get("login");
        }
        return name;
    }

    @Override
    public String getPicture() {
        return (String) attributes.get("avatar_url");
    }
}
