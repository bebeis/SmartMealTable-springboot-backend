package com.stcom.smartmealtable.web.auth.social;

import com.stcom.smartmealtable.web.dto.token.TokenDto;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.RequestBodySpec;
import org.springframework.web.client.RestClient.ResponseSpec;

@Component
public class SocialManager {

    private final Map<String, SocialHttpMessage> socialMap = new HashMap<>();

    public SocialManager() {
        socialMap.put("Kakao", new KakaoHttpMessage());
    }

    public RequestBodySpec getTokenRequestMessage(RestClient client, String provider, String code) {
        return socialMap.get(provider).getRequestMessage(client, code);
    }

    public TokenDto getTokenResponse(ResponseSpec responseSpec, String provider) {
        return socialMap.get(provider).getTokenResponse(responseSpec);
    }
}
