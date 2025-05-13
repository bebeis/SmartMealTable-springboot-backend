package com.stcom.smartmealtable.domain.social;

import com.stcom.smartmealtable.common.BaseTimeEntity;
import com.stcom.smartmealtable.domain.member.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
public class SocialAccount extends BaseTimeEntity {

    @Id
    @GeneratedValue
    @Column(name = "social_account_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private String provider;

    private String providerUserId;

    private String tokenType;

    private String accessToken;

    private String refreshToken;

    private LocalDateTime tokenExpiresAt;

    @Builder
    public SocialAccount(Member member, String provider, String providerUserId, String tokenType,
                         String accessToken, String refreshToken, LocalDateTime tokenExpiresAt) {
        this.member = member;
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.tokenType = tokenType;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenExpiresAt = tokenExpiresAt;
    }

    public void updateToken(String accessToken, String refreshToken, LocalDateTime tokenExpiresAt) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenExpiresAt = tokenExpiresAt;
    }
}
