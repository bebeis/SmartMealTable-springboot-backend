package com.stcom.smartmealtable.infrastructure;

import static com.stcom.smartmealtable.infrastructure.social.SocialConst.GOOGLE;
import static com.stcom.smartmealtable.infrastructure.social.SocialConst.KAKAO;

import com.stcom.smartmealtable.exception.ExternApiStatusError;
import com.stcom.smartmealtable.infrastructure.dto.TokenDto;
import com.stcom.smartmealtable.infrastructure.social.GoogleHttpMessage;
import com.stcom.smartmealtable.infrastructure.social.KakaoHttpMessage;
import com.stcom.smartmealtable.infrastructure.social.SocialHttpMessage;
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
        try {
            ResponseSpec responseSpec = socialMap.get(provider).getRequestMessage(client, code).retrieve();
            return socialMap.get(provider).getTokenResponse(responseSpec);
        } catch (RuntimeException e) {
            throw new ExternApiStatusError("소셜 로그인 서드파티 Api 호출 중 예외가 발생했습니다.", e);
        }
    }

    @PostConstruct
    public void init() {
        socialMap.put(KAKAO, kakaoHttpMessage);
        socialMap.put(GOOGLE, googleHttpMessage);
    }

}
