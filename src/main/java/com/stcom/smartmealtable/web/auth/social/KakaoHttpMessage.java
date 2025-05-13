package com.stcom.smartmealtable.web.auth.social;

import static com.stcom.smartmealtable.web.auth.social.SocialConst.KAKAO;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.stcom.smartmealtable.web.dto.token.TokenDto;
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
public class KakaoHttpMessage implements SocialHttpMessage {

    @Value("${kakao.oauth.client-id}")
    private String clientId;

    @Value("${kakao.oauth.redirect-uri}")
    private String redirectUri;

    @Override
    public RequestBodySpec getRequestMessage(RestClient client, String code) {
        return client.post()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("kauth.kakao.com")
                        .path("/oauth/token")
                        .build())
                // form data 로 보내려면 반드시 URL_ENCODED
                .headers(h -> h.setContentType(MediaType.APPLICATION_FORM_URLENCODED))
                .body(createFormData(code));
    }

    private MultiValueMap<String, String> createFormData(String code) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("client_id", clientId);
        formData.add("redirect_uri", redirectUri);
        formData.add("code", code);
        log.info("client Id = {}", clientId);
        return formData;
    }


    @Override
    public TokenDto getTokenResponse(ResponseSpec responseSpec) {
        KakaoTokenResponse tokenResponse = responseSpec.body(KakaoTokenResponse.class);
        return TokenDto.builder()
                .accessToken(tokenResponse.getAccessToken())
                .refreshToken(tokenResponse.getRefreshToken())
                .expiresIn(tokenResponse.getExpiresIn())
                .tokenType(tokenResponse.getTokenType())
                .provider(KAKAO)
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

//    @Data
//    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
//    static class KakaoTokenRequest {
//
//        public KakaoTokenRequest(String code) {
//            this.code = code;
//        }
//
//        @NotBlank
//        private String grantType = "authorization_code";
//
//        @NotBlank
//        @Value("${kakao.oauth.client-id}")
//        private String clientId;
//
//        @NotBlank
//        @Value("${kakao.oauth.redirect-uri}")
//        private String redirectUri;
//
//        @NotBlank
//        private String code;
//    }

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
