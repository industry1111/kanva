package com.kanva.auth.oauth.provider;

public interface OAuth2UserInfo {

    String getProvider();

    String getProviderUserId();

    String getEmail();

    String getName();

    String getPicture();
}
