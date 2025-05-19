package com.stcom.smartmealtable.service;

import com.stcom.smartmealtable.domain.member.Member;
import com.stcom.smartmealtable.domain.member.MemberProfile;
import com.stcom.smartmealtable.repository.MemberRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public void validateDuplicatedEmail(String email) {
        List<Member> findMembers = memberRepository.findMemberByEmail(email);
        if (!findMembers.isEmpty()) {
            throw new IllegalArgumentException("이미 존재하는 이메일 입니다");
        }
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
}
