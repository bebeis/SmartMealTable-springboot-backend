package com.stcom.smartmealtable.web.dto.token;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtTokenResponseDto {

    private String accessToken;
    private String refreshToken;
    private int expiresIn;
    private String tokenType;
    private boolean isNewUser;
}
