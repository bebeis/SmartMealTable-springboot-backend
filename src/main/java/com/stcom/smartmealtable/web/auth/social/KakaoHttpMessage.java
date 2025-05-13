package com.stcom.smartmealtable.web.auth.social;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.stcom.smartmealtable.web.dto.token.TokenDto;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.RequestBodySpec;
import org.springframework.web.client.RestClient.ResponseSpec;

public class KakaoHttpMessage implements SocialHttpMessage {

    @Override
    public RequestBodySpec getRequestMessage(RestClient client, String code) {
        return client.post()
                .uri(uriBuilder -> uriBuilder.path("https://kauth.kakao.com/oauth/token").build())
                .headers(httpHeaders -> httpHeaders.setContentType(MediaType.APPLICATION_JSON))
                .body(new KakaoTokenRequest(code));
    }

    @Override
    public TokenDto getTokenResponse(ResponseSpec responseSpec) {
        KakaoTokenResponse tokenResponse = responseSpec.body(KakaoTokenResponse.class);
        return TokenDto.builder()
                .accessToken(tokenResponse.getAccessToken())
                .refreshToken(tokenResponse.getRefreshToken())
                .expiresIn(tokenResponse.getExpiresIn())
                .tokenType(tokenResponse.getTokenType())
                .provider("Kakao")
                .providerUserId(extractProviderUserId(tokenResponse.getIdToken()))
                .build();

    }

    @Override
    public String extractProviderUserId(String idToken) {
        if (idToken == null || idToken.isEmpty()) {
            return null;
        }

        try {
            String[] jwtParts = idToken.split("\\.");
            if (jwtParts.length != 3) {
                return null;
            }

            String payload = new String(java.util.Base64.getUrlDecoder().decode(jwtParts[1]));

            // Jackson ObjectMapper를 사용하여 JSON 파싱
            ObjectMapper mapper = new ObjectMapper();
            JsonNode payloadJson = mapper.readTree(payload);

            // 'sub' 필드가 사용자 ID임
            return payloadJson.has("sub") ? payloadJson.get("sub").asText() : null;
        } catch (Exception e) {
            // 예외 발생 시 로깅 및 null 반환
            System.err.println("카카오 ID 토큰 파싱 오류: " + e.getMessage());
            return null;
        }
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    static class KakaoTokenRequest {

        public KakaoTokenRequest(String code) {
            this.code = code;
        }

        @NotBlank
        private String grantType = "authorization_code";

        @NotBlank
        @Value("${kakao.oauth.client-id}")
        private String clientId;

        @NotBlank
        @Value("${kakao.oauth.redirect-uri}")
        private String redirectUri;

        @NotBlank
        private String code;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    static class KakaoTokenResponse {

        private String tokenType;

        private String accessToken;

        private String idToken;

        private Integer expiresIn;

        private String refreshToken;

        private Integer refreshTokenExpiresIn;

    }
}
