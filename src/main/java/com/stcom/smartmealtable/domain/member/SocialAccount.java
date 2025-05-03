package com.stcom.smartmealtable.domain.member;

import com.stcom.smartmealtable.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;

@Entity
public class SocialAccount extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "social_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private MemberAuth memberAuth;

    private SocialLoginProvider provider;

    private String providerUserId;

    private String accessToken;

    private String refreshToken;

    private LocalDateTime tokenExpiresAt;
}
