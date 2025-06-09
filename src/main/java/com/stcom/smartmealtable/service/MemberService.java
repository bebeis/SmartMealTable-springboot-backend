package com.stcom.smartmealtable.service;

import com.stcom.smartmealtable.domain.member.Member;
import com.stcom.smartmealtable.exception.PasswordFailedExceededException;
import com.stcom.smartmealtable.exception.PasswordPolicyException;
import com.stcom.smartmealtable.repository.AddressEntityRepository;
import com.stcom.smartmealtable.repository.MemberProfileRepository;
import com.stcom.smartmealtable.repository.MemberRepository;
import com.stcom.smartmealtable.repository.SocialAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberProfileRepository memberProfileRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final AddressEntityRepository addressEntityRepository;

    /**
     * 이메일 중복 검사를 수행합니다. 동일한 이메일을 가진 회원이 이미 존재하면 예외를 발생시킵니다.
     *
     * @param email 중복 검사할 이메일
     * @throws IllegalArgumentException 이메일이 이미 존재하는 경우
     */
    public void validateDuplicatedEmail(String email) {
        memberRepository.findByEmail(email).ifPresent(member -> {
            throw new IllegalArgumentException("이미 존재하는 이메일 입니다");
        });
    }

    @Transactional
    public void saveMember(Member member) {
        memberRepository.save(member);
    }

    public Member findMemberByMemberId(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다"));
    }

    public void checkPasswordDoubly(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("검증 비밀번호가 일치하지 않습니다");
        }
    }

    public void changePassword(Long memberId, String originPassword, String newPassword)
            throws PasswordFailedExceededException, PasswordPolicyException {
        Member findMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다"));
        findMember.changePassword(originPassword, newPassword);
    }

    public void deleteByMemberId(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다"));
        memberProfileRepository.deleteMemberProfileByMember(member);
        socialAccountRepository.deleteSocialAccountByMember(member);
        memberRepository.delete(member);
    }
}
