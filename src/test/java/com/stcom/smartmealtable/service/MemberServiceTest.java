package com.stcom.smartmealtable.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stcom.smartmealtable.domain.member.Member;
import com.stcom.smartmealtable.exception.PasswordFailedExceededException;
import com.stcom.smartmealtable.exception.PasswordPolicyException;
import com.stcom.smartmealtable.repository.AddressEntityRepository;
import com.stcom.smartmealtable.repository.MemberProfileRepository;
import com.stcom.smartmealtable.repository.MemberRepository;
import com.stcom.smartmealtable.repository.SocialAccountRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberProfileRepository memberProfileRepository;

    @Mock
    private SocialAccountRepository socialAccountRepository;

    @Mock
    private AddressEntityRepository addressEntityRepository;

    @InjectMocks
    private MemberService memberService;

    @Test
    @DisplayName("회원 정보를 정상적으로 저장할 수 있어야 한다")
    void saveMember() throws PasswordPolicyException {
        // given
        Member member = Member.builder()
                .email("test@example.com")
                .fullName("홍길동")
                .rawPassword("Password123!")
                .build();

        when(memberRepository.save(any(Member.class))).thenReturn(member);

        // when
        memberService.saveMember(member);

        // then
        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    @DisplayName("ID로 회원 조회 시 회원이 존재하면 회원 정보를 반환해야 한다")
    void findMemberByMemberId() throws PasswordPolicyException {
        // given
        Long memberId = 1L;
        Member member = Member.builder()
                .email("test@example.com")
                .fullName("홍길동")
                .rawPassword("Password123!")
                .build();

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        // when
        Member foundMember = memberService.findMemberByMemberId(memberId);

        // then
        assertThat(foundMember).isEqualTo(member);
        verify(memberRepository, times(1)).findById(memberId);
    }

    @Test
    @DisplayName("ID로 회원 조회 시 회원이 존재하지 않으면 예외가 발생해야 한다")
    void findMemberByMemberIdNotFound() {
        // given
        Long memberId = 999L;
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.findMemberByMemberId(memberId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 회원입니다");
    }

    @Test
    @DisplayName("비밀번호 확인이 일치하지 않으면 예외가 발생해야 한다")
    void checkPasswordDoublyFail() {
        // given
        String password = "Password123!";
        String confirmPassword = "DifferentPassword123!";

        // when & then
        assertThatThrownBy(() -> memberService.checkPasswordDoubly(password, confirmPassword))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비밀번호가 일치하지 않습니다");
    }

    @Test
    @DisplayName("비밀번호 변경이 정상적으로 동작해야 한다")
    void changePassword() throws PasswordPolicyException, PasswordFailedExceededException {
        // given
        Long memberId = 1L;
        String originPassword = "OriginPassword123!";
        String newPassword = "NewPassword123!";

        Member member = Member.builder()
                .email("test@example.com")
                .fullName("홍길동")
                .rawPassword(originPassword)
                .build();

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        // when
        memberService.changePassword(memberId, originPassword, newPassword);

        // then
        assertThat(member.isMatchedPassword(newPassword)).isTrue();
    }

    @Test
    @DisplayName("회원 삭제가 정상적으로 동작해야 한다")
    void deleteByMemberId() throws PasswordPolicyException {
        // given
        Long memberId = 1L;
        Member member = Member.builder()
                .email("test@example.com")
                .fullName("홍길동")
                .rawPassword("Password123!")
                .build();

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        doNothing().when(memberProfileRepository).deleteMemberProfileByMember(any(Member.class));
        doNothing().when(socialAccountRepository).deleteSocialAccountByMember(any(Member.class));
        doNothing().when(memberRepository).delete(any(Member.class));

        // when
        memberService.deleteByMemberId(memberId);

        // then
        verify(memberProfileRepository, times(1)).deleteMemberProfileByMember(member);
        verify(socialAccountRepository, times(1)).deleteSocialAccountByMember(member);
        verify(memberRepository, times(1)).delete(member);
    }
} 