package com.stcom.smartmealtable.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stcom.smartmealtable.domain.member.Member;
import com.stcom.smartmealtable.domain.social.SocialAccount;
import com.stcom.smartmealtable.infrastructure.dto.TokenDto;
import com.stcom.smartmealtable.repository.MemberRepository;
import com.stcom.smartmealtable.repository.SocialAccountRepository;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
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
class SocialAccountServiceTest {

    @Mock
    private SocialAccountRepository socialAccountRepository;
    
    @Mock
    private MemberRepository memberRepository;
    
    @InjectMocks
    private SocialAccountService socialAccountService;
    
    @Captor
    private ArgumentCaptor<Member> memberCaptor;
    
    @Captor
    private ArgumentCaptor<SocialAccount> socialAccountCaptor;

    @Test
    @DisplayName("새 회원 생성 및 소셜 계정 연결이 가능해야 한다")
    void createNewMemberAndLinkSocialAccount() {
        // given
        TokenDto tokenDto = createTokenDto("test@example.com", "KAKAO", "12345");
        
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> {
            Member member = invocation.getArgument(0);
            ReflectionTestUtils.setField(member, "id", 1L);
            return member;
        });
        
        when(socialAccountRepository.save(any(SocialAccount.class))).thenAnswer(invocation -> {
            SocialAccount account = invocation.getArgument(0);
            ReflectionTestUtils.setField(account, "id", 1L);
            return account;
        });

        // when
        socialAccountService.createNewMemberAndLinkSocialAccount(tokenDto);

        // then
        verify(memberRepository, times(1)).save(memberCaptor.capture());
        verify(socialAccountRepository, times(1)).save(socialAccountCaptor.capture());
        
        Member savedMember = memberCaptor.getValue();
        SocialAccount savedAccount = socialAccountCaptor.getValue();
        
        assertThat(savedMember.getEmail()).isEqualTo("test@example.com");
        assertThat(savedAccount.getMember()).isEqualTo(savedMember);
        assertThat(savedAccount.getProvider()).isEqualTo("KAKAO");
        assertThat(savedAccount.getProviderUserId()).isEqualTo("12345");
        assertThat(savedAccount.getAccessToken()).isEqualTo("access-token-value");
        assertThat(savedAccount.getRefreshToken()).isEqualTo("refresh-token-value");
    }
    
    @Test
    @DisplayName("기존 회원에 소셜 계정 연결이 가능해야 한다")
    void linkSocialAccount() {
        // given
        String email = "test@example.com";
        TokenDto tokenDto = createTokenDto(email, "GOOGLE", "67890");
        
        Member existingMember = new Member(email);
        ReflectionTestUtils.setField(existingMember, "id", 1L);
        
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(existingMember));
        when(socialAccountRepository.save(any(SocialAccount.class))).thenAnswer(invocation -> {
            SocialAccount account = invocation.getArgument(0);
            ReflectionTestUtils.setField(account, "id", 2L);
            return account;
        });

        // when
        socialAccountService.linkSocialAccount(tokenDto);

        // then
        verify(memberRepository, times(1)).findByEmail(email);
        verify(socialAccountRepository, times(1)).save(socialAccountCaptor.capture());
        
        SocialAccount savedAccount = socialAccountCaptor.getValue();
        
        assertThat(savedAccount.getMember()).isEqualTo(existingMember);
        assertThat(savedAccount.getProvider()).isEqualTo("GOOGLE");
        assertThat(savedAccount.getProviderUserId()).isEqualTo("67890");
        assertThat(savedAccount.getAccessToken()).isEqualTo("access-token-value");
    }
    
    @Test
    @DisplayName("존재하지 않는 이메일로 소셜 계정 연결 시 예외가 발생해야 한다")
    void linkSocialAccountWithNonExistingEmail() {
        // given
        String email = "nonexisting@example.com";
        TokenDto tokenDto = createTokenDto(email, "KAKAO", "12345");
        
        when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> socialAccountService.linkSocialAccount(tokenDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("회원이 null일 수는 없습니다");
    }
    
    @Test
    @DisplayName("Provider와 ProviderUserId로 소셜 계정을 찾을 수 있어야 한다")
    void findSocialAccount() {
        // given
        String provider = "KAKAO";
        String providerUserId = "12345";
        
        Member member = new Member("test@example.com");
        ReflectionTestUtils.setField(member, "id", 1L);
        
        SocialAccount socialAccount = SocialAccount.builder()
                .member(member)
                .provider(provider)
                .providerUserId(providerUserId)
                .build();
        ReflectionTestUtils.setField(socialAccount, "id", 1L);
        
        when(socialAccountRepository.findByProviderAndProviderUserId(provider, providerUserId))
                .thenReturn(Optional.of(socialAccount));

        // when
        SocialAccount foundAccount = socialAccountService.findSocialAccount(provider, providerUserId);

        // then
        assertThat(foundAccount).isNotNull();
        assertThat(foundAccount.getProvider()).isEqualTo(provider);
        assertThat(foundAccount.getProviderUserId()).isEqualTo(providerUserId);
        assertThat(foundAccount.getMember()).isEqualTo(member);
        
        verify(socialAccountRepository, times(1)).findByProviderAndProviderUserId(provider, providerUserId);
    }
    
    @Test
    @DisplayName("존재하지 않는 소셜 계정 찾기 시 null을 반환해야 한다")
    void findNonExistingSocialAccount() {
        // given
        String provider = "KAKAO";
        String providerUserId = "nonexisting";
        
        when(socialAccountRepository.findByProviderAndProviderUserId(provider, providerUserId))
                .thenReturn(Optional.empty());

        // when
        SocialAccount foundAccount = socialAccountService.findSocialAccount(provider, providerUserId);

        // then
        assertThat(foundAccount).isNull();
    }
    
    @Test
    @DisplayName("신규 사용자 여부를 확인할 수 있어야 한다")
    void isNewUser() {
        // given
        String existingProvider = "KAKAO";
        String existingProviderId = "12345";
        
        String newProvider = "GOOGLE";
        String newProviderId = "67890";
        
        when(socialAccountRepository.findByProviderAndProviderUserId(existingProvider, existingProviderId))
                .thenReturn(Optional.of(new SocialAccount()));
        
        when(socialAccountRepository.findByProviderAndProviderUserId(newProvider, newProviderId))
                .thenReturn(Optional.empty());

        // when
        boolean existingUserResult = socialAccountService.isNewUser(existingProvider, existingProviderId);
        boolean newUserResult = socialAccountService.isNewUser(newProvider, newProviderId);

        // then
        assertThat(existingUserResult).isFalse();
        assertThat(newUserResult).isTrue();
    }
    
    @Test
    @DisplayName("토큰 정보를 업데이트할 수 있어야 한다")
    void updateToken() {
        // given
        Long socialAccountId = 1L;
        
        Member member = new Member("test@example.com");
        SocialAccount socialAccount = SocialAccount.builder()
                .member(member)
                .provider("KAKAO")
                .providerUserId("12345")
                .tokenType("Bearer")
                .accessToken("old-access-token")
                .refreshToken("old-refresh-token")
                .tokenExpiresAt(LocalDateTime.now())
                .build();
        ReflectionTestUtils.setField(socialAccount, "id", socialAccountId);
        
        String newAccessToken = "new-access-token";
        String newRefreshToken = "new-refresh-token";
        LocalDateTime newExpiresAt = LocalDateTime.now().plusHours(1);
        
        when(socialAccountRepository.findById(socialAccountId)).thenReturn(Optional.of(socialAccount));

        // when
        socialAccountService.updateToken(socialAccountId, newAccessToken, newRefreshToken, newExpiresAt);

        // then
        verify(socialAccountRepository, times(1)).findById(socialAccountId);
        
        assertThat(socialAccount.getAccessToken()).isEqualTo(newAccessToken);
        assertThat(socialAccount.getRefreshToken()).isEqualTo(newRefreshToken);
        assertThat(socialAccount.getTokenExpiresAt()).isEqualTo(newExpiresAt);
    }
    
    @Test
    @DisplayName("존재하지 않는 소셜 계정 ID로 토큰 업데이트 시 예외가 발생해야 한다")
    void updateTokenWithNonExistingId() {
        // given
        Long nonExistingId = 999L;
        
        when(socialAccountRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> socialAccountService.updateToken(
                nonExistingId, "new-token", "new-refresh-token", LocalDateTime.now()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("확인되지 않은 계정입니다");
    }
    
    @Test
    @DisplayName("회원 ID로 연결된 모든 소셜 Provider 목록을 가져올 수 있어야 한다")
    void findAllProviders() {
        // given
        Long memberId = 1L;
        
        Member member = new Member("test@example.com");
        
        SocialAccount kakaoAccount = SocialAccount.builder()
                .member(member)
                .provider("KAKAO")
                .providerUserId("kakao-12345")
                .build();
                
        SocialAccount googleAccount = SocialAccount.builder()
                .member(member)
                .provider("GOOGLE")
                .providerUserId("google-67890")
                .build();
        
        List<SocialAccount> socialAccounts = Arrays.asList(kakaoAccount, googleAccount);
        
        when(socialAccountRepository.findAllByMemberId(memberId)).thenReturn(socialAccounts);

        // when
        List<String> providers = socialAccountService.findAllProviders(memberId);

        // then
        assertThat(providers).hasSize(2);
        assertThat(providers).containsExactly("KAKAO", "GOOGLE");
        
        verify(socialAccountRepository, times(1)).findAllByMemberId(memberId);
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