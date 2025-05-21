package com.stcom.smartmealtable.service;

import com.stcom.smartmealtable.domain.Address.Address;
import com.stcom.smartmealtable.domain.Address.AddressEntity;
import com.stcom.smartmealtable.domain.Address.AddressType;
import com.stcom.smartmealtable.domain.group.Group;
import com.stcom.smartmealtable.domain.member.Member;
import com.stcom.smartmealtable.domain.member.MemberProfile;
import com.stcom.smartmealtable.domain.member.MemberType;
import com.stcom.smartmealtable.repository.AddressEntityRepository;
import com.stcom.smartmealtable.repository.GroupRepository;
import com.stcom.smartmealtable.repository.MemberProfileRepository;
import com.stcom.smartmealtable.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberProfileService {

    private final MemberProfileRepository memberProfileRepository;
    private final GroupRepository groupRepository;
    private final MemberRepository memberRepository;
    private final AddressEntityRepository addressEntityRepository;

    public MemberProfile getProfileFetch(Long profileId) {
        return memberProfileRepository.findMemberProfileEntityGraphById(profileId)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 프로필입니다"));
    }

    @Transactional
    public void createProfile(String nickName, Long memberId, MemberType type, Long groupId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다"));

        // TODO: 실제 프록시 객체 초기화 시점에 인스턴스의 서브타입이 결정된다는데, 테스트해보기
        Group group = (groupId != null)
                ? groupRepository.getReferenceById(groupId)
                : null;

        MemberProfile profile = MemberProfile.builder()
                .nickName(nickName)
                .member(member)
                .type(type)
                .group(group)
                .build();

        memberProfileRepository.save(profile);
    }

    @Transactional
    public void changeProfile(Long profileId, String nickName, MemberType type, Long groupId) {
        MemberProfile profile = memberProfileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 프로필입니다"));

        profile.changeNickName(nickName);
        profile.changeMemberType(type);
        Group newGroup = (groupId != null)
                ? groupRepository.getReferenceById(groupId)
                : null;
        profile.changeGroup(newGroup);
    }

    @Transactional
    public void changeAddressToPrimary(Long profileId, Long addressId) {
        MemberProfile profile = memberProfileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 프로필입니다"));
        AddressEntity targetAddressEntity = addressEntityRepository.findById(addressId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원 주소 정보입니다."));
        profile.setPrimaryAddress(targetAddressEntity);
    }

    @Transactional
    public void saveNewAddress(Long profileId, Address address, String alias, AddressType addressType) {
        MemberProfile profile = memberProfileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 프로필입니다"));
        AddressEntity addressEntity = AddressEntity.builder()
                .address(address)
                .alias(alias)
                .type(addressType)
                .build();
        addressEntityRepository.save(addressEntity);
        profile.addAddress(addressEntity);
    }
}
