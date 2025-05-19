package com.stcom.smartmealtable.service;

import com.stcom.smartmealtable.domain.member.Member;
import com.stcom.smartmealtable.domain.social.SocialAccount;
import com.stcom.smartmealtable.repository.MemberRepository;
import com.stcom.smartmealtable.repository.SocialAccountRepository;
import com.stcom.smartmealtable.service.dto.token.TokenDto;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SocialAccountService {

    private final SocialAccountRepository socialAccountRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void createNewAccount(TokenDto tokenDto) {
        Member member = new Member();
        memberRepository.save(member);

        SocialAccount socialAccount = SocialAccount.builder()
                .member(member)
                .provider(tokenDto.getProvider())
                .providerUserId(tokenDto.getProviderUserId())
                .tokenType(tokenDto.getTokenType())
                .accessToken(tokenDto.getAccessToken())
                .refreshToken(tokenDto.getRefreshToken())
                .tokenExpiresAt(LocalDateTime.now().plusSeconds(tokenDto.getExpiresIn()))
                .build();
        socialAccountRepository.save(socialAccount);
    }

    public SocialAccount findSocialAccount(String provider, String providerUserId) {
        return socialAccountRepository.findByProviderAndProviderUserId(provider, providerUserId).orElse(null);
    }

    public boolean isNewUser(String provider, String providerUserId) {
        return socialAccountRepository.findByProviderAndProviderUserId(provider, providerUserId).isEmpty();
    }

    @Transactional
    public void updateToken(Long socialAccountId, String accessToken, String refreshToken,
                            LocalDateTime tokenExpiresAt) {
        SocialAccount socialAccount = socialAccountRepository.findById(socialAccountId)
                .orElseThrow(() -> new IllegalStateException("확인되지 않은 계정입니다"));
        socialAccount.updateToken(accessToken, refreshToken, tokenExpiresAt);
    }

    public Long findMemberId(String provider, String providerUserId) {
        return socialAccountRepository.findMemberIdByProviderAndProviderUserId(provider, providerUserId)
                .orElseThrow(() -> new IllegalStateException("회원 정보가 없습니다. 먼저 회원 정보를 생성해주세요"));
    }
}
