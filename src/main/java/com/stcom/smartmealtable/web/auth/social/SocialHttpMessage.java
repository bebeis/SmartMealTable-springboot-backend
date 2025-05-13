package com.stcom.smartmealtable.web.auth.social;

import com.stcom.smartmealtable.web.dto.token.TokenDto;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.RequestBodySpec;
import org.springframework.web.client.RestClient.ResponseSpec;

public interface SocialHttpMessage {

    RequestBodySpec getRequestMessage(RestClient client, String code);

    TokenDto getTokenResponse(ResponseSpec responseSpec);

    String extractProviderUserId(String idToken);
}
