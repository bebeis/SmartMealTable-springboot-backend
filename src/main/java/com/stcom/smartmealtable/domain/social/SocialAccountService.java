package com.stcom.smartmealtable.domain.social;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SocialAccountService {

    private final SocialAccountRepository socialAccountRepository;

    public SocialAccount getSocialAccount(String provider, String providerUserId) {
        return socialAccountRepository.findByProviderAndProviderUserId(provider, providerUserId)
                .orElse(null);
    }

    @Transactional
    public void updateToken(SocialAccount socialAccount, String accessToken, String refreshToken,
                            LocalDateTime tokenExpiresAt) {
        socialAccount.updateToken(accessToken, refreshToken, tokenExpiresAt);
        socialAccountRepository.save(socialAccount);
    }

}
