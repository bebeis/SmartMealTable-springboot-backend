package com.stcom.smartmealtable.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.stcom.smartmealtable.domain.Address.Address;
import com.stcom.smartmealtable.domain.Address.AddressEntity;
import com.stcom.smartmealtable.domain.Address.AddressType;
import com.stcom.smartmealtable.domain.member.Member;
import com.stcom.smartmealtable.domain.member.MemberProfile;
import com.stcom.smartmealtable.domain.member.MemberType;
import com.stcom.smartmealtable.repository.AddressEntityRepository;
import com.stcom.smartmealtable.repository.MemberProfileRepository;
import com.stcom.smartmealtable.repository.MemberRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class MemberProfileServiceAdditionalIntegrationTest2 {

    @Autowired
    private MemberProfileService service;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberProfileRepository profileRepository;

    @Autowired
    private AddressEntityRepository addressEntityRepository;

    private Member member;
    private MemberProfile profile;
    private AddressEntity addressEntity;

    @BeforeEach
    void setUp() {
        // 테스트용 회원 생성
        member = Member.builder()
                .email("profile_additional2@example.com")
                .rawPassword("password123!")
                .build();
        memberRepository.save(member);

        // 프로필 생성
        profile = MemberProfile.builder()
                .member(member)
                .nickName("프로필유저")
                .type(MemberType.STUDENT)
                .build();
        profileRepository.save(profile);

        // 주소 생성 및 추가
        Address address = Address.builder()
                .roadAddress("서울시 강남구")
                .detailAddress("123번지")
                .build();
        
        service.saveNewAddress(profile.getId(), address, "집", AddressType.HOME);
        
        // 저장된 주소 조회
        profile = profileRepository.findById(profile.getId()).orElseThrow();
        addressEntity = profile.getAddressHistory().get(0);
    }

    @Test
    @DisplayName("주소 정보를 수정할 수 있다")
    void changeAddress() {
        // given
        Address newAddress = Address.builder()
                .roadAddress("서울시 서초구")
                .detailAddress("456번지")
                .build();
        String newAlias = "새집";
        AddressType newType = AddressType.OFFICE;

        // when
        service.changeAddress(profile.getId(), addressEntity.getId(), newAddress, newAlias, newType);

        // then
        MemberProfile updatedProfile = profileRepository.findById(profile.getId()).orElseThrow();
        AddressEntity updatedAddressEntity = updatedProfile.getAddressHistory().get(0);
        
        assertThat(updatedAddressEntity.getAddress().getRoadAddress()).isEqualTo("서울시 서초구");
        assertThat(updatedAddressEntity.getAddress().getDetailAddress()).isEqualTo("456번지");
        assertThat(updatedAddressEntity.getAlias()).isEqualTo("새집");
        assertThat(updatedAddressEntity.getType()).isEqualTo(AddressType.OFFICE);
    }

    @Test
    @DisplayName("존재하지 않는 프로필 ID로 getProfileFetch 호출 시 예외가 발생한다")
    void getProfileFetchWithInvalidId() {
        // given
        Long invalidProfileId = 99999L;

        // when & then
        assertThatThrownBy(() -> service.getProfileFetch(invalidProfileId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 프로필입니다");
    }

    @Test
    @DisplayName("존재하지 않는 회원 ID로 프로필 생성 시 예외가 발생한다")
    void createProfileWithInvalidMemberId() {
        // given
        Long invalidMemberId = 99999L;

        // when & then
        assertThatThrownBy(() -> service.createProfile("테스트", invalidMemberId, MemberType.STUDENT, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 회원입니다");
    }

    @Test
    @DisplayName("존재하지 않는 프로필 ID로 프로필 변경 시 예외가 발생한다")
    void changeProfileWithInvalidId() {
        // given
        Long invalidProfileId = 99999L;

        // when & then
        assertThatThrownBy(() -> service.changeProfile(invalidProfileId, "변경", MemberType.WORKER, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 프로필입니다");
    }

    @Test
    @DisplayName("존재하지 않는 프로필 ID로 기본 주소 변경 시 예외가 발생한다")
    void changeAddressToPrimaryWithInvalidProfileId() {
        // given
        Long invalidProfileId = 99999L;

        // when & then
        assertThatThrownBy(() -> service.changeAddressToPrimary(invalidProfileId, addressEntity.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 프로필입니다");
    }

    @Test
    @DisplayName("존재하지 않는 주소 ID로 기본 주소 변경 시 예외가 발생한다")
    void changeAddressToPrimaryWithInvalidAddressId() {
        // given
        Long invalidAddressId = 99999L;

        // when & then
        assertThatThrownBy(() -> service.changeAddressToPrimary(profile.getId(), invalidAddressId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 회원 주소 정보입니다.");
    }

    @Test
    @DisplayName("존재하지 않는 프로필 ID로 주소 생성 시 예외가 발생한다")
    void saveNewAddressWithInvalidProfileId() {
        // given
        Long invalidProfileId = 99999L;
        Address address = Address.builder()
                .roadAddress("서울시")
                .detailAddress("123번지")
                .build();

        // when & then
        assertThatThrownBy(() -> service.saveNewAddress(invalidProfileId, address, "집", AddressType.HOME))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 프로필입니다");
    }

    @Test
    @DisplayName("존재하지 않는 프로필 ID로 주소 변경 시 예외가 발생한다")
    void changeAddressWithInvalidProfileId() {
        // given
        Long invalidProfileId = 99999L;
        Address address = Address.builder()
                .roadAddress("서울시")
                .detailAddress("123번지")
                .build();

        // when & then
        assertThatThrownBy(() -> service.changeAddress(invalidProfileId, addressEntity.getId(), address, "집", AddressType.HOME))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 프로필입니다");
    }

    @Test
    @DisplayName("존재하지 않는 주소 ID로 주소 변경 시 예외가 발생한다")
    void changeAddressWithInvalidAddressId() {
        // given
        Long invalidAddressId = 99999L;
        Address address = Address.builder()
                .roadAddress("서울시")
                .detailAddress("123번지")
                .build();

        // when & then
        assertThatThrownBy(() -> service.changeAddress(profile.getId(), invalidAddressId, address, "집", AddressType.HOME))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 회원 주소 정보입니다.");
    }

    @Test
    @DisplayName("존재하지 않는 프로필 ID로 주소 삭제 시 예외가 발생한다")
    void deleteAddressWithInvalidProfileId() {
        // given
        Long invalidProfileId = 99999L;

        // when & then
        assertThatThrownBy(() -> service.deleteAddress(invalidProfileId, addressEntity.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 프로필입니다");
    }

    @Test
    @DisplayName("존재하지 않는 주소 ID로 주소 삭제 시 예외가 발생한다")
    void deleteAddressWithInvalidAddressId() {
        // given
        Long invalidAddressId = 99999L;

        // when & then
        assertThatThrownBy(() -> service.deleteAddress(profile.getId(), invalidAddressId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 회원 주소 정보입니다.");
    }
} 