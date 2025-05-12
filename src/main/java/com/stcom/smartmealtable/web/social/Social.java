package com.stcom.smartmealtable.web.social;

import com.stcom.smartmealtable.web.dto.token.TokenDto;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.RequestBodySpec;
import org.springframework.web.client.RestClient.ResponseSpec;

public interface Social {

    RequestBodySpec getRequestMessage(RestClient client, String code);

    TokenDto getTokenResponse(ResponseSpec responseSpec);

    String extractProviderUserId(String idToken);
}
