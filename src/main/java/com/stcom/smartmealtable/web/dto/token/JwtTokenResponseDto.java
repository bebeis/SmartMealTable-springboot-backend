package com.stcom.smartmealtable.web.dto.token;

import lombok.Data;

@Data
public class JwtTokenResponseDto {

    private String accessToken;
    private String refreshToken;
    private int expiresIn;
    private String tokenType;
    private boolean isNewUser;

    public JwtTokenResponseDto(String accessToken, String refreshToken, int expiresIn, String tokenType) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.tokenType = tokenType;
    }
}
