package com.stcom.smartmealtable.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.stcom.smartmealtable.domain.Address.Address;
import com.stcom.smartmealtable.domain.Address.AddressType;
import com.stcom.smartmealtable.domain.member.Member;
import com.stcom.smartmealtable.domain.member.MemberProfile;
import com.stcom.smartmealtable.domain.member.MemberType;
import com.stcom.smartmealtable.repository.MemberProfileRepository;
import com.stcom.smartmealtable.repository.MemberRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class MemberProfileServiceAdditionalIntegrationTest {

    @Autowired
    private MemberProfileService service;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberProfileRepository profileRepository;

    @Test
    @DisplayName("프로필 닉네임/타입/그룹 변경이 동작해야 한다")
    void changeProfile() {
        Member member = new Member("cp@test.com");
        memberRepository.save(member);
        MemberProfile profile = MemberProfile.builder()
                .member(member)
                .nickName("old")
                .type(MemberType.STUDENT)
                .group(null)
                .build();
        profileRepository.save(profile);

        // when
        service.changeProfile(profile.getId(), "newNick", MemberType.WORKER, null);

        // then
        MemberProfile updated = profileRepository.findById(profile.getId()).orElseThrow();
        assertThat(updated.getNickName()).isEqualTo("newNick");
        assertThat(updated.getType()).isEqualTo(MemberType.WORKER);
    }

    @Test
    @DisplayName("주소 삭제가 동작해야 한다")
    void deleteAddress() {
        Member member = new Member("addr@test.com");
        memberRepository.save(member);
        MemberProfile profile = MemberProfile.builder()
                .member(member)
                .nickName("a")
                .type(MemberType.STUDENT)
                .group(null)
                .build();
        profileRepository.save(profile);

        Address address = Address.builder().roadAddress("road").detailAddress("d").build();
        service.saveNewAddress(profile.getId(), address, "집", AddressType.HOME);
        Long addressId = profileRepository.findById(profile.getId()).orElseThrow().getAddressHistory().get(0).getId();

        // when
        service.deleteAddress(profile.getId(), addressId);

        // then
        MemberProfile after = profileRepository.findById(profile.getId()).orElseThrow();
        assertThat(after.getAddressHistory()).isEmpty();
    }
} 