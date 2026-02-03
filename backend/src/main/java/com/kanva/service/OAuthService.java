package com.kanva.service;

import com.kanva.domain.user.OAuthProvider;
import com.kanva.dto.auth.OAuthCallbackRequest;
import com.kanva.dto.auth.OAuthLoginUrlResponse;
import com.kanva.dto.user.LoginResponse;

public interface OAuthService {

    OAuthLoginUrlResponse getLoginUrl(OAuthProvider provider);

    LoginResponse processCallback(OAuthProvider provider, OAuthCallbackRequest request);
}
