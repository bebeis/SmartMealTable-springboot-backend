package com.stcom.smartmealtable.service;

import com.stcom.smartmealtable.domain.member.Member;
import com.stcom.smartmealtable.domain.member.MemberProfile;
import com.stcom.smartmealtable.repository.MemberProfileRepository;
import com.stcom.smartmealtable.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberProfileRepository memberProfileRepository;

    public void validateDuplicatedEmail(String email) {
        memberRepository.findMemberByEmail(email).orElseThrow(() -> new IllegalArgumentException("이미 존재하는 이메일 입니다"));
    }

    @Transactional
    public void saveMember(Member member) {
        memberRepository.save(member);
    }

    public void checkPasswordDoubly(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다");
        }
    }

    @Transactional
    public void linkMember(Long id, MemberProfile profile) {
        Member member = memberRepository.findById(id).orElseThrow(() -> new IllegalStateException("존재하지 않는 회원입니다"));
        member.registerMemberProfile(profile);
    }

    public MemberProfile findMemberProfileByMemberId(Long memberId) {
        return memberProfileRepository.findMemberProfileByMemberId(memberId)
                .orElseThrow(() -> new IllegalStateException("프로필이 없는 유저를 조회하였습니다."));

    }

    public boolean isNewMember(String email) {
        return memberRepository.findMemberByEmail(email).isEmpty();
    }
}
