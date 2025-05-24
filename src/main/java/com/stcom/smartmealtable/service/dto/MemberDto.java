package com.stcom.smartmealtable.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberDto {
    private Long memberId;
    private Long profileId;
    private String email;

    public static MemberDto createFrom(Long memberId, Long profileId, String email) {
        return MemberDto.builder()
                .memberId(memberId)
                .profileId(profileId)
                .email(email)
                .build();
    }
} 