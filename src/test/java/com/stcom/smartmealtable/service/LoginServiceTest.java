package com.stcom.smartmealtable.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stcom.smartmealtable.domain.member.Member;
import com.stcom.smartmealtable.domain.member.MemberProfile;
import com.stcom.smartmealtable.domain.social.SocialAccount;
import com.stcom.smartmealtable.exception.PasswordPolicyException;
import com.stcom.smartmealtable.exception.PasswordFailedExceededException;
import com.stcom.smartmealtable.infrastructure.dto.TokenDto;
import com.stcom.smartmealtable.repository.MemberRepository;
import com.stcom.smartmealtable.repository.SocialAccountRepository;
import com.stcom.smartmealtable.service.dto.AuthResultDto;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private SocialAccountRepository socialAccountRepository;

    @InjectMocks
    private LoginService loginService;

    @Captor
    private ArgumentCaptor<Member> memberCaptor;

    @Captor
    private ArgumentCaptor<SocialAccount> socialAccountCaptor;

    @Test
    @DisplayName("이메일과 비밀번호로 로그인이 가능해야 한다")
    void loginWithEmail() throws PasswordFailedExceededException, PasswordPolicyException {
        // given
        String email = "test@example.com";
        String password = "Password123!";
        
        Member member = createMember(1L, email, password);
        MemberProfile profile = mock(MemberProfile.class);
        when(profile.getId()).thenReturn(10L);

        // 프로필이 등록되어 있지 않은 경우
        ReflectionTestUtils.setField(member, "memberProfile", profile);
        
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
        
        // when
        AuthResultDto result = loginService.loginWithEmail(email, password);
        
        // then
        assertThat(result.getMemberId()).isEqualTo(1L);
        assertThat(result.getProfileId()).isEqualTo(10L);
        assertThat(result.isNewUser()).isFalse(); // 프로필이 있으므로 새 사용자가 아님

        verify(memberRepository, times(1)).findByEmail(email);
    }
    
    @Test
    @DisplayName("프로필이 없는 경우 신규 사용자로 처리해야 한다")
    void loginWithEmailNewUser() throws PasswordFailedExceededException, PasswordPolicyException {
        // given
        String email = "new@example.com";
        String password = "Password123!";
        
        Member member = createMember(2L, email, password);
        // 프로필이 등록되어 있지 않음
        
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
        
        // when
        AuthResultDto result = loginService.loginWithEmail(email, password);
        
        // then
        assertThat(result.getMemberId()).isEqualTo(2L);
        assertThat(result.getProfileId()).isNull();
        assertThat(result.isNewUser()).isTrue(); // 프로필이 없으므로 신규 사용자
    }
    
    @Test
    @DisplayName("존재하지 않는 이메일로 로그인 시도 시 예외가 발생해야 한다")
    void loginWithNonExistingEmail() {
        // given
        String email = "nonexisting@example.com";
        String password = "Password123!";
        
        when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());
        
        // when & then
        assertThatThrownBy(() -> loginService.loginWithEmail(email, password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 회원입니다.");
    }
    
    @Test
    @DisplayName("잘못된 비밀번호로 로그인 시도 시 예외가 발생해야 한다")
    void loginWithWrongPassword() throws PasswordPolicyException {
        // given
        String email = "test@example.com";
        String correctPassword = "Password123!";
        String wrongPassword = "WrongPassword123!";
        
        Member member = createMember(1L, email, correctPassword);
        
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
        
        // when & then
        assertThatThrownBy(() -> loginService.loginWithEmail(email, wrongPassword))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비밀번호가 일치하지 않습니다");
    }
    
    @Test
    @DisplayName("소셜 로그인 - 기존 회원인 경우")
    void socialLoginExistingMember() throws PasswordPolicyException {
        // given
        String email = "test@example.com";
        String provider = "KAKAO";
        String providerUserId = "12345";

        TokenDto tokenDto = createTokenDto(email, provider, providerUserId);
        Member member = createMember(1L, email, null);

        SocialAccount existingSocialAccount = SocialAccount.builder()
                .member(member)
                .provider(provider)
                .providerUserId(providerUserId)
                .accessToken("old-token")
                .refreshToken("old-refresh-token")
                .tokenExpiresAt(LocalDateTime.now())
                .build();

        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
        when(socialAccountRepository.findByProviderAndProviderUserId(provider, providerUserId))
                .thenReturn(Optional.of(existingSocialAccount));
        when(socialAccountRepository.findProfileIdByProviderAndProviderUserId(provider, providerUserId))
                .thenReturn(Optional.of(10L));
        
        // when
        AuthResultDto result = loginService.socialLogin(tokenDto);
        
        // then
        assertThat(result.getMemberId()).isEqualTo(1L);
        assertThat(result.getProfileId()).isEqualTo(10L);
        assertThat(result.isNewUser()).isFalse();

        assertThat(existingSocialAccount.getAccessToken()).isEqualTo("access-token-value");
        assertThat(existingSocialAccount.getRefreshToken()).isEqualTo("refresh-token-value");
    }
    
    @Test
    @DisplayName("소셜 로그인 - 신규 회원인 경우")
    void socialLoginNewMember() throws PasswordPolicyException {
        // given
        String email = "new@example.com";
        String provider = "GOOGLE";
        String providerUserId = "67890";
        
        TokenDto tokenDto = createTokenDto(email, provider, providerUserId);
        Member newMember = createMember(2L, email, null);
        
        when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(memberRepository.save(any())).thenReturn(newMember);
        when(socialAccountRepository.findByProviderAndProviderUserId(provider, providerUserId))
                .thenReturn(Optional.empty());
        when(socialAccountRepository.save(any())).thenAnswer(invocation -> {
            SocialAccount account = invocation.getArgument(0);
            ReflectionTestUtils.setField(account, "id", 1L);
            return account;
        });
        when(socialAccountRepository.findProfileIdByProviderAndProviderUserId(provider, providerUserId))
                .thenReturn(Optional.empty());
        
        // when
        AuthResultDto result = loginService.socialLogin(tokenDto);
        
        // then
        verify(memberRepository, times(1)).save(memberCaptor.capture());
        verify(socialAccountRepository, times(1)).save(socialAccountCaptor.capture());

        Member savedMember = memberCaptor.getValue();
        SocialAccount savedAccount = socialAccountCaptor.getValue();

        assertThat(savedMember.getEmail()).isEqualTo(email);
        assertThat(savedAccount.getProvider()).isEqualTo(provider);
        assertThat(savedAccount.getProviderUserId()).isEqualTo(providerUserId);

        assertThat(result.getMemberId()).isEqualTo(2L);
        assertThat(result.getProfileId()).isNull();
        assertThat(result.isNewUser()).isTrue();
    }
    
    private Member createMember(Long id, String email, String rawPassword) throws PasswordPolicyException {
        Member member;
        if (rawPassword != null) {
            member = Member.builder()
                    .email(email)
                    .rawPassword(rawPassword)
                    .build();
        } else {
            member = new Member(email);
        }
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }
    
    private TokenDto createTokenDto(String email, String provider, String providerUserId) {
        TokenDto tokenDto = new TokenDto();

        tokenDto.setEmail(email);
        tokenDto.setProvider(provider);
        tokenDto.setProviderUserId(providerUserId);
        tokenDto.setTokenType("Bearer");
        tokenDto.setAccessToken("access-token-value");
        tokenDto.setRefreshToken("refresh-token-value");
        tokenDto.setExpiresIn(3600);

        return tokenDto;
    }
} 