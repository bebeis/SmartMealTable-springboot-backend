package com.stcom.smartmealtable.service.dto.token;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TokenDto {

    private String accessToken;
    private String refreshToken;
    private Integer expiresIn;
    private String tokenType;
    private String provider;
    private String providerUserId;

    @Builder
    public TokenDto(String accessToken, String refreshToken, Integer expiresIn, String tokenType, String provider,
                    String providerUserId) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.tokenType = tokenType;
        this.provider = provider;
        this.providerUserId = providerUserId;
    }
}
