package com.stcom.smartmealtable.service;

import com.stcom.smartmealtable.domain.member.Member;
import com.stcom.smartmealtable.domain.social.SocialAccount;
import com.stcom.smartmealtable.infrastructure.dto.TokenDto;
import com.stcom.smartmealtable.repository.MemberRepository;
import com.stcom.smartmealtable.repository.SocialAccountRepository;
import java.time.LocalDateTime;
import java.util.List;
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
    public void createNewMemberAndLinkSocialAccount(TokenDto tokenDto) {
        Member member = new Member(tokenDto.getEmail());
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

    @Transactional
    public void linkSocialAccount(TokenDto tokenDto) {
        Member member = memberRepository.findByEmail(tokenDto.getEmail())
                .orElseThrow(() -> new IllegalStateException("회원 엔티티가 존재하지 않은 상태로 소셜 계정 연결을 시도했습니다."));

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
                .orElseThrow(() -> new IllegalArgumentException("확인되지 않은 계정입니다"));
        socialAccount.updateToken(accessToken, refreshToken, tokenExpiresAt);
    }

    public List<String> findAllProviders(Long memberId) {
        List<SocialAccount> accounts = socialAccountRepository.findAllByMemberId(memberId);
        return accounts.stream()
                .map(SocialAccount::getProvider)
                .toList();
    }


}
