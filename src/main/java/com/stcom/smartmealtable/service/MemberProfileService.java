package com.stcom.smartmealtable.service;

import com.stcom.smartmealtable.domain.group.Group;
import com.stcom.smartmealtable.domain.group.GroupType;
import com.stcom.smartmealtable.domain.member.Member;
import com.stcom.smartmealtable.domain.member.MemberProfile;
import com.stcom.smartmealtable.repository.GroupRepository;
import com.stcom.smartmealtable.repository.MemberProfileRepository;
import com.stcom.smartmealtable.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberProfileService {

    private final MemberProfileRepository memberProfileRepository;
    private final GroupRepository groupRepository;
    private final MemberRepository memberRepository;

    public MemberProfile getProfileFetch(Long profileId) {
        return memberProfileRepository.findMemberProfileEntityGraphById(profileId)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 프로필입니다"));
    }

    @Transactional
    public void createProfile(String nickName, Long memberId, GroupType groupType, Long groupId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다"));

        // TODO: 실제 프록시 객체 초기화 시점에 인스턴스의 서브타입이 결정된다는데, 테스트해보기
        Group group = (groupId != null)
                ? groupRepository.getReferenceById(groupId)
                : null;

        MemberProfile profile = MemberProfile.builder()
                .nickName(nickName)
                .member(member)
                .group(group)
                .build();

        memberProfileRepository.save(profile);
    }
}
