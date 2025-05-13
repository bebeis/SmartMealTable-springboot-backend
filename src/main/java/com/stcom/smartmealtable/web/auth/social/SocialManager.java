package com.stcom.smartmealtable.web.auth.social;

import static com.stcom.smartmealtable.web.auth.social.SocialConst.KAKAO;

import com.stcom.smartmealtable.web.dto.token.TokenDto;
import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.RequestBodySpec;
import org.springframework.web.client.RestClient.ResponseSpec;

@Component
@RequiredArgsConstructor
public class SocialManager {

    private final Map<String, SocialHttpMessage> socialMap = new HashMap<>();
    private final KakaoHttpMessage kakaoHttpMessage;

    public RequestBodySpec getTokenRequestMessage(RestClient client, String provider, String code) {
        return socialMap.get(provider).getRequestMessage(client, code);
    }

    public TokenDto getTokenResponse(ResponseSpec responseSpec, String provider) {
        return socialMap.get(provider).getTokenResponse(responseSpec);
    }

    @PostConstruct
    public void init() {
        socialMap.put(KAKAO, kakaoHttpMessage);
    }
}
