package com.stcom.smartmealtable.infrastructure.social;

import static com.stcom.smartmealtable.infrastructure.social.SocialConst.GOOGLE;
import static com.stcom.smartmealtable.infrastructure.social.SocialConst.KAKAO;

import com.stcom.smartmealtable.service.dto.token.TokenDto;
import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotEmpty;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.ResponseSpec;

@Service
@RequiredArgsConstructor
public class SocialAuthService {

    private final Map<String, SocialHttpMessage> socialMap = new HashMap<>();
    private final KakaoHttpMessage kakaoHttpMessage;
    private final GoogleHttpMessage googleHttpMessage;
    private final RestClient client = RestClient.create();

    public TokenDto getTokenResponse(@NotEmpty String provider, @NotEmpty String code) {
        ResponseSpec responseSpec = socialMap.get(provider).getRequestMessage(client, code).retrieve();
        return socialMap.get(provider).getTokenResponse(responseSpec);
    }

    @PostConstruct
    public void init() {
        socialMap.put(KAKAO, kakaoHttpMessage);
        socialMap.put(GOOGLE, googleHttpMessage);
    }


}
