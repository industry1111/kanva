package com.kanva.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanva.config.OAuthConfig;
import com.kanva.domain.user.OAuthProvider;
import com.kanva.domain.user.Role;
import com.kanva.domain.user.User;
import com.kanva.domain.user.UserRepository;
import com.kanva.dto.auth.OAuthCallbackRequest;
import com.kanva.dto.auth.OAuthLoginUrlResponse;
import com.kanva.dto.user.LoginResponse;
import com.kanva.dto.user.UserResponse;
import com.kanva.exception.OAuthException;
import com.kanva.security.jwt.JwtToken;
import com.kanva.security.jwt.JwtTokenProvider;
import com.kanva.service.OAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OAuthServiceImpl implements OAuthService {

    private final OAuthConfig oAuthConfig;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    // state 저장소 (실제 프로덕션에서는 Redis 사용 권장)
    private final Map<String, Long> stateStore = new ConcurrentHashMap<>();
    private static final long STATE_EXPIRATION_MS = 10 * 60 * 1000; // 10분

    @Override
    public OAuthLoginUrlResponse getLoginUrl(OAuthProvider provider) {
        String state = UUID.randomUUID().toString();
        stateStore.put(state, System.currentTimeMillis());

        String url = switch (provider) {
            case GITHUB -> buildGitHubAuthUrl(state);
            case SLACK -> buildSlackAuthUrl(state);
        };

        return OAuthLoginUrlResponse.builder()
                .url(url)
                .state(state)
                .build();
    }

    @Override
    @Transactional
    public LoginResponse processCallback(OAuthProvider provider, OAuthCallbackRequest request) {
        validateState(request.getState());

        User user = switch (provider) {
            case GITHUB -> processGitHubCallback(request.getCode());
            case SLACK -> processSlackCallback(request.getCode());
        };

        JwtToken jwtToken = jwtTokenProvider.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );

        return LoginResponse.builder()
                .accessToken(jwtToken.getAccessToken())
                .refreshToken(jwtToken.getRefreshToken())
                .tokenType(jwtToken.getGrantType())
                .user(UserResponse.from(user))
                .build();
    }

    private void validateState(String state) {
        Long timestamp = stateStore.remove(state);
        if (timestamp == null) {
            throw new OAuthException("유효하지 않은 상태 값입니다.");
        }
        if (System.currentTimeMillis() - timestamp > STATE_EXPIRATION_MS) {
            throw new OAuthException("상태 값이 만료되었습니다.");
        }
    }

    // === GitHub OAuth ===

    private String buildGitHubAuthUrl(String state) {
        OAuthConfig.ProviderConfig config = oAuthConfig.getGithub();
        return "https://github.com/login/oauth/authorize"
                + "?client_id=" + config.getClientId()
                + "&redirect_uri=" + encode(config.getRedirectUri())
                + "&scope=" + encode("user:email")
                + "&state=" + state;
    }

    private User processGitHubCallback(String code) {
        String accessToken = exchangeGitHubCode(code);
        JsonNode userInfo = getGitHubUserInfo(accessToken);

        String providerId = userInfo.get("id").asText();
        String name = userInfo.has("name") && !userInfo.get("name").isNull()
                ? userInfo.get("name").asText()
                : userInfo.get("login").asText();
        String email = getGitHubEmail(accessToken, userInfo);
        String picture = userInfo.has("avatar_url") && !userInfo.get("avatar_url").isNull()
                ? userInfo.get("avatar_url").asText()
                : null;

        return findOrCreateUser(OAuthProvider.GITHUB, providerId, email, name, picture);
    }

    private String exchangeGitHubCode(String code) {
        OAuthConfig.ProviderConfig config = oAuthConfig.getGithub();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Accept", "application/json");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", config.getClientId());
        params.add("client_secret", config.getClientSecret());
        params.add("code", code);
        params.add("redirect_uri", config.getRedirectUri());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "https://github.com/login/oauth/access_token",
                    request,
                    String.class
            );

            JsonNode json = objectMapper.readTree(response.getBody());
            if (json.has("error")) {
                throw new OAuthException("GitHub 인증 실패: " + json.get("error_description").asText());
            }
            return json.get("access_token").asText();
        } catch (OAuthException e) {
            throw e;
        } catch (Exception e) {
            log.error("GitHub 코드 교환 실패", e);
            throw new OAuthException("GitHub 인증 처리 중 오류가 발생했습니다.", e);
        }
    }

    private JsonNode getGitHubUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.set("Accept", "application/json");

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    "https://api.github.com/user",
                    HttpMethod.GET,
                    request,
                    String.class
            );
            return objectMapper.readTree(response.getBody());
        } catch (Exception e) {
            log.error("GitHub 사용자 정보 조회 실패", e);
            throw new OAuthException("GitHub 사용자 정보를 가져올 수 없습니다.", e);
        }
    }

    private String getGitHubEmail(String accessToken, JsonNode userInfo) {
        // 먼저 기본 정보에서 email 확인
        if (userInfo.has("email") && !userInfo.get("email").isNull()) {
            return userInfo.get("email").asText();
        }

        // email이 없으면 /user/emails API 호출
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.set("Accept", "application/json");

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    "https://api.github.com/user/emails",
                    HttpMethod.GET,
                    request,
                    String.class
            );
            JsonNode emails = objectMapper.readTree(response.getBody());

            // primary email 찾기
            for (JsonNode emailNode : emails) {
                if (emailNode.get("primary").asBoolean() && emailNode.get("verified").asBoolean()) {
                    return emailNode.get("email").asText();
                }
            }

            // primary가 없으면 첫 번째 verified email
            for (JsonNode emailNode : emails) {
                if (emailNode.get("verified").asBoolean()) {
                    return emailNode.get("email").asText();
                }
            }

            throw new OAuthException("GitHub에서 이메일을 가져올 수 없습니다. 이메일 공개 설정을 확인해주세요.");
        } catch (OAuthException e) {
            throw e;
        } catch (Exception e) {
            log.error("GitHub 이메일 조회 실패", e);
            throw new OAuthException("GitHub 이메일을 가져올 수 없습니다.", e);
        }
    }

    // === Slack OAuth ===

    private String buildSlackAuthUrl(String state) {
        OAuthConfig.ProviderConfig config = oAuthConfig.getSlack();
        return "https://slack.com/oauth/v2/authorize"
                + "?client_id=" + config.getClientId()
                + "&redirect_uri=" + encode(config.getRedirectUri())
                + "&user_scope=" + encode("identity.basic,identity.email,identity.avatar")
                + "&state=" + state;
    }

    private User processSlackCallback(String code) {
        JsonNode tokenResponse = exchangeSlackCode(code);
        JsonNode userInfo = tokenResponse.get("authed_user");

        String userAccessToken = userInfo.get("access_token").asText();
        JsonNode identity = getSlackIdentity(userAccessToken);

        JsonNode user = identity.get("user");
        String providerId = user.get("id").asText();
        String name = user.get("name").asText();
        String email = user.get("email").asText();
        String picture = user.has("image_192") ? user.get("image_192").asText() : null;

        return findOrCreateUser(OAuthProvider.SLACK, providerId, email, name, picture);
    }

    private JsonNode exchangeSlackCode(String code) {
        OAuthConfig.ProviderConfig config = oAuthConfig.getSlack();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", config.getClientId());
        params.add("client_secret", config.getClientSecret());
        params.add("code", code);
        params.add("redirect_uri", config.getRedirectUri());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "https://slack.com/api/oauth.v2.access",
                    request,
                    String.class
            );

            JsonNode json = objectMapper.readTree(response.getBody());
            if (!json.get("ok").asBoolean()) {
                throw new OAuthException("Slack 인증 실패: " + json.get("error").asText());
            }
            return json;
        } catch (OAuthException e) {
            throw e;
        } catch (Exception e) {
            log.error("Slack 코드 교환 실패", e);
            throw new OAuthException("Slack 인증 처리 중 오류가 발생했습니다.", e);
        }
    }

    private JsonNode getSlackIdentity(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    "https://slack.com/api/users.identity",
                    HttpMethod.GET,
                    request,
                    String.class
            );

            JsonNode json = objectMapper.readTree(response.getBody());
            if (!json.get("ok").asBoolean()) {
                throw new OAuthException("Slack 사용자 정보 조회 실패: " + json.get("error").asText());
            }
            return json;
        } catch (OAuthException e) {
            throw e;
        } catch (Exception e) {
            log.error("Slack 사용자 정보 조회 실패", e);
            throw new OAuthException("Slack 사용자 정보를 가져올 수 없습니다.", e);
        }
    }

    // === 공통 ===

    private User findOrCreateUser(OAuthProvider provider, String providerId, String email, String name, String picture) {
        return userRepository.findByOauthProviderAndOauthProviderId(provider, providerId)
                .map(user -> {
                    user.updateOAuthInfo(name, email, picture);
                    return user;
                })
                .orElseGet(() -> {
                    // 같은 이메일로 이미 가입된 계정이 있는지 확인
                    return userRepository.findByEmail(email)
                            .map(existingUser -> {
                                // 기존 계정에 OAuth 정보 연결
                                existingUser.linkOAuth(provider, providerId, picture);
                                return existingUser;
                            })
                            .orElseGet(() -> {
                                // 신규 사용자 생성
                                User newUser = User.oauthBuilder()
                                        .email(email)
                                        .name(name)
                                        .oauthProvider(provider)
                                        .oauthProviderId(providerId)
                                        .picture(picture)
                                        .role(Role.USER)
                                        .build();
                                return userRepository.save(newUser);
                            });
                });
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
