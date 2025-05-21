package com.stcom.smartmealtable.service;

import com.stcom.smartmealtable.domain.member.Member;
import com.stcom.smartmealtable.domain.social.SocialAccount;
import com.stcom.smartmealtable.exception.PasswordFailedExceededException;
import com.stcom.smartmealtable.infrastructure.dto.TokenDto;
import com.stcom.smartmealtable.repository.MemberRepository;
import com.stcom.smartmealtable.repository.SocialAccountRepository;
import com.stcom.smartmealtable.service.dto.AuthResultDto;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final MemberRepository memberRepository;
    private final SocialAccountRepository socialAccountRepository;

    public AuthResultDto loginWithEmail(String email, String password) throws PasswordFailedExceededException {
        Member findMember = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        if (!findMember.isMatchedPassword(password)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다");
        }
        boolean newUser = findMember.isProfileRegistered();
        Long profileId = newUser ? null : findMember.getMemberProfile().getId();
        return new AuthResultDto(findMember.getId(), profileId, newUser);
    }

    @Transactional
    public AuthResultDto socialLogin(TokenDto token) {
        Member member = memberRepository.findByEmail(token.getEmail())
                .orElseGet(() -> memberRepository.save(new Member(token.getEmail())));

        SocialAccount sa = socialAccountRepository.findByProviderAndProviderUserId(
                        token.getProvider(), token.getProviderUserId())
                .map(existing -> {
                    existing.updateToken(
                            token.getAccessToken(),
                            token.getRefreshToken(),
                            LocalDateTime.now().plusSeconds(token.getExpiresIn()));
                    return existing;
                })
                .orElseGet(() -> socialAccountRepository.save(
                        SocialAccount.builder()
                                .member(member)
                                .provider(token.getProvider())
                                .providerUserId(token.getProviderUserId())
                                .tokenType(token.getTokenType())
                                .accessToken(token.getAccessToken())
                                .refreshToken(token.getRefreshToken())
                                .tokenExpiresAt(LocalDateTime.now().plusSeconds(token.getExpiresIn()))
                                .build()
                ));

        Long profileId = socialAccountRepository
                .findProfileIdByProviderAndProviderUserId(token.getProvider(), token.getProviderUserId())
                .orElse(null);
        boolean newUser = (profileId == null);
        return new AuthResultDto(member.getId(), profileId, newUser);
    }

}
