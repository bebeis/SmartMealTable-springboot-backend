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
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class MemberProfileServiceIntegrationTest {

    @Autowired
    private MemberProfileService memberProfileService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberProfileRepository memberProfileRepository;

    @Test
    @DisplayName("프로필 생성 후 주소 추가 및 기본 주소 설정이 가능해야 한다")
    @Rollback
    void createAndUpdateProfile() throws Exception {
        // given 회원 저장
        Member member = Member.builder()
                .fullName("사용자A")
                .email("usera@example.com")
                .rawPassword("Password1!")
                .build();
        memberRepository.save(member);

        // when 프로필 생성
        memberProfileService.createProfile("닉네임", member.getId(), MemberType.STUDENT, null);
        MemberProfile profile = memberProfileRepository.findAll().get(0);

        // 주소 추가
        Address address = Address.builder()
                .roadAddress("서울 특별시 강남구 역삼동")
                .detailAddress("101호")
                .build();
        memberProfileService.saveNewAddress(profile.getId(), address, "집", AddressType.HOME);

        // 기본 주소 설정
        Long addressEntityId = profile.getAddressHistory().get(0).getId();
        memberProfileService.changeAddressToPrimary(profile.getId(), addressEntityId);

        // then
        MemberProfile updated = memberProfileService.getProfileFetch(profile.getId());
        assertThat(updated.findPrimaryAddress().getAlias()).isEqualTo("집");
    }
} 