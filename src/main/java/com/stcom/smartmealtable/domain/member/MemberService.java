package com.stcom.smartmealtable.domain.member;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public boolean isEmailExists(String email) {
        List<Member> findMembers = memberRepository.findMemberByEmail(email);
        if (findMembers.isEmpty()) {
            return false;
        }
        return true;
    }

    public void saveMember(Member member) {
        memberRepository.save(member);
    }
}
