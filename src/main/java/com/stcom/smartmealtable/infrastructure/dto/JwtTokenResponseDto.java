package com.stcom.smartmealtable.infrastructure.dto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
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
