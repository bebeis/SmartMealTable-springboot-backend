package com.stcom.smartmealtable.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResultDto {
    private Long memberId;
    private Long profileId;
    private boolean newUser;
} 