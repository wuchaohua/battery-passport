package com.battery.auth.infrastructure.adapter;

import com.battery.common.BatteryException;
import com.battery.security.SsoAdapter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TrinaOidcSsoAdapter implements SsoAdapter {

    @Value("${battery.sso.trina.iam-server-url:https://iam.trinasolar.com}")
    private String iamServerUrl;

    @Value("${battery.sso.trina.client-id:battery-passport}")
    private String clientId;

    @Value("${battery.sso.trina.client-secret:}")
    private String clientSecret;

    @Value("${battery.sso.trina.redirect-uri:https://battery.trinasolar.com/api/v1/auth/callback}")
    private String redirectUri;

    @Value("${battery.sso.trina.scope:openid profile email}")
    private String scope;

    @Value("${battery.sso.trina.response-type:code}")
    private String responseType;

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public TrinaOidcSsoAdapter() {
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newHttpClient();
    }

    @Override
    public String protocol() {
        return "OIDC";
    }

    @Override
    public String buildLoginUrl(String redirectUri) {
        String actualRedirectUri = (redirectUri != null && !redirectUri.isEmpty()) ? redirectUri : this.redirectUri;
        Map<String, String> params = new HashMap<>();
        params.put("response_type", responseType);
        params.put("client_id", clientId);
        params.put("redirect_uri", actualRedirectUri);
        params.put("scope", scope);
        return iamServerUrl + "/oidc/endpoint/default/authorize?" + buildQueryString(params);
    }

    @Override
    public PlatformUser handleCallback(String code, String state) {
        if (code == null || code.isEmpty()) {
            throw new BatteryException("OIDC authorization code is required");
        }

        String tokenEndpoint = iamServerUrl + "/oidc/endpoint/default/token";
        Map<String, String> params = new HashMap<>();
        params.put("grant_type", "authorization_code");
        params.put("code", code);
        params.put("client_id", clientId);
        params.put("client_secret", clientSecret);
        params.put("redirect_uri", redirectUri);

        String tokenResponse = sendPostRequest(tokenEndpoint, params);
        String accessToken = extractToken(tokenResponse, "access_token");

        String userInfoEndpoint = iamServerUrl + "/oidc/endpoint/default/userinfo";
        String userInfoResponse = sendGetRequestWithBearer(userInfoEndpoint, accessToken);

        return parseUserInfo(userInfoResponse);
    }

    @Override
    public PlatformUser validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }

        String userInfoEndpoint = iamServerUrl + "/oidc/endpoint/default/userinfo";
        try {
            String userInfoResponse = sendGetRequestWithBearer(userInfoEndpoint, token);
            return parseUserInfo(userInfoResponse);
        } catch (Exception e) {
            return null;
        }
    }

    private PlatformUser parseUserInfo(String userInfoResponse) {
        try {
            JsonNode root = objectMapper.readTree(userInfoResponse);
            PlatformUser user = new PlatformUser();

            String userId = getTextValue(root, "sub");
            if (userId == null || userId.isEmpty()) {
                userId = getTextValue(root, "uid");
            }
            if (userId == null || userId.isEmpty()) {
                userId = getTextValue(root, "userid");
            }
            user.setUserId(userId);

            String username = getTextValue(root, "preferred_username");
            if (username == null || username.isEmpty()) {
                username = getTextValue(root, "username");
            }
            if (username == null || username.isEmpty()) {
                username = userId;
            }
            user.setUsername(username);

            String displayName = getTextValue(root, "name");
            if (displayName == null || displayName.isEmpty()) {
                displayName = getTextValue(root, "displayName");
            }
            if (displayName == null || displayName.isEmpty()) {
                displayName = getTextValue(root, "given_name");
                String familyName = getTextValue(root, "family_name");
                if (familyName != null && !familyName.isEmpty()) {
                    displayName = (displayName != null ? displayName : "") + " " + familyName;
                }
            }
            if (displayName == null || displayName.isEmpty()) {
                displayName = username;
            }
            user.setDisplayName(displayName.trim());

            String email = getTextValue(root, "email");
            if (email == null || email.isEmpty()) {
                email = getTextValue(root, "mail");
            }
            user.setEmail(email);

            user.setEnterpriseCode("trina");

            Map<String, Object> attributes = new HashMap<>();
            root.fields().forEachRemaining(entry -> {
                String key = entry.getKey();
                JsonNode value = entry.getValue();
                if (value.isTextual()) {
                    attributes.put(key, value.asText());
                } else if (value.isNumber()) {
                    attributes.put(key, value.asLong());
                } else if (value.isBoolean()) {
                    attributes.put(key, value.asBoolean());
                } else if (value.isArray()) {
                    attributes.put(key, objectMapper.convertValue(value, java.util.List.class));
                } else {
                    attributes.put(key, value.toString());
                }
            });
            user.setAttributes(attributes);

            return user;
        } catch (IOException e) {
            throw new BatteryException("Failed to parse user info: " + e.getMessage());
        }
    }

    private String extractToken(String response, String tokenType) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode tokenNode = root.get(tokenType);
            if (tokenNode != null && !tokenNode.isNull()) {
                return tokenNode.asText();
            }
            throw new BatteryException("Token not found in response");
        } catch (IOException e) {
            throw new BatteryException("Failed to parse token response: " + e.getMessage());
        }
    }

    private String sendPostRequest(String url, Map<String, String> params) {
        String body = buildQueryString(params);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new BatteryException("OIDC request failed with status " + response.statusCode() + ": " + response.body());
            }
            return response.body();
        } catch (IOException | InterruptedException e) {
            throw new BatteryException("Failed to send request: " + e.getMessage());
        }
    }

    private String sendGetRequestWithBearer(String url, String token) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new BatteryException("OIDC request failed with status " + response.statusCode());
            }
            return response.body();
        } catch (IOException | InterruptedException e) {
            throw new BatteryException("Failed to send request: " + e.getMessage());
        }
    }

    private String buildQueryString(Map<String, String> params) {
        return params.entrySet().stream()
                .filter(e -> e.getValue() != null && !e.getValue().isEmpty())
                .map(e -> e.getKey() + "=" + encodeUrl(e.getValue()))
                .collect(Collectors.joining("&"));
    }

    private String encodeUrl(String url) {
        try {
            return java.net.URLEncoder.encode(url, "UTF-8");
        } catch (Exception e) {
            return url;
        }
    }

    private String getTextValue(JsonNode node, String fieldName) {
        if (node == null || !node.has(fieldName)) {
            return null;
        }
        JsonNode field = node.get(fieldName);
        if (field == null || field.isNull()) {
            return null;
        }
        return field.asText(null);
    }
}