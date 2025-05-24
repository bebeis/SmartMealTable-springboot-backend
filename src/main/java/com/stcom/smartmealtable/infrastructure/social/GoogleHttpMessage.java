package com.stcom.smartmealtable.infrastructure.social;

import static com.stcom.smartmealtable.infrastructure.social.SocialConst.GOOGLE;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.stcom.smartmealtable.infrastructure.dto.TokenDto;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.RequestBodySpec;
import org.springframework.web.client.RestClient.ResponseSpec;

@Component
@Slf4j
public class GoogleHttpMessage implements SocialHttpMessage {

    @Value("${google.oauth.client-id}")
    private String clientId;

    @Value("${google.oauth.client-secret}")
    private String clientSecret;

    @Value("${google.oauth.redirect-uri}")
    private String redirectUri;


    @Override
    public RequestBodySpec getRequestMessage(RestClient client, String code) {
        return client.post()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("oauth2.googleapis.com")
                        .path("/token")
                        .build())
                // form data 로 보내려면 반드시 URL_ENCODED
                .headers(h -> h.setContentType(MediaType.APPLICATION_FORM_URLENCODED))
                .body(createFormData(code));
    }

    private MultiValueMap<String, String> createFormData(String code) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("code", code);
        formData.add("redirect_uri", redirectUri);
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        log.info("client Id = {}", clientId);
        return formData;
    }

    @Override
    public TokenDto getTokenResponse(ResponseSpec responseSpec) {
        GoogleTokenResponse tokenResponse = responseSpec.body(GoogleTokenResponse.class);
        return TokenDto.builder()
                .accessToken(tokenResponse.getAccessToken())
                .refreshToken(tokenResponse.getRefreshToken())
                .expiresIn(tokenResponse.getExpiresIn())
                .tokenType(tokenResponse.getTokenType())
                .provider(GOOGLE)
                .providerUserId(extractProviderUserId(tokenResponse.getIdToken()))
                .email(extractEmail(tokenResponse.getIdToken()))
                .build();
    }

    @Override
    public String extractProviderUserId(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            return null;
        }
        try {
            String[] jwtParts = idToken.split("\\.");
            if (jwtParts.length != 3) {
                return null;
            }

            String payload = new String(Base64.getUrlDecoder().decode(jwtParts[1]), StandardCharsets.UTF_8);
            JsonNode payloadJson = new ObjectMapper().readTree(payload);
            return payloadJson.path("sub").asText(null);

        } catch (Exception e) {
            // 로깅: 어떤 공급자(token issuer)에 대한 토큰인지 같이 찍어도 좋습니다.
            log.error("ID 토큰 파싱 오류: {}", e.getMessage());
            return null;
        }
    }

    public String extractEmail(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            return null;
        }
        try {
            String[] parts = idToken.split("\\.");
            if (parts.length != 3) {
                return null;
            }
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            JsonNode node = new ObjectMapper().readTree(payload);
            return node.has("email") ? node.get("email").asText() : null;
        } catch (Exception e) {
            log.error("Google ID 토큰에서 email 파싱 실패: {}", e.getMessage());
            return null;
        }
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    static class GoogleTokenResponse {

        private String accessToken;
        private Integer expiresIn;
        private String refreshToken;
        private String scope;
        private String tokenType;
        private String idToken;
    }
}
