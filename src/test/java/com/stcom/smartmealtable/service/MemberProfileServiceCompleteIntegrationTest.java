package com.stcom.smartmealtable.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.stcom.smartmealtable.domain.Address.Address;
import com.stcom.smartmealtable.domain.Address.AddressEntity;
import com.stcom.smartmealtable.domain.Address.AddressType;
import com.stcom.smartmealtable.domain.group.Group;
import com.stcom.smartmealtable.domain.group.SchoolGroup;
import com.stcom.smartmealtable.domain.group.SchoolType;
import com.stcom.smartmealtable.domain.member.Member;
import com.stcom.smartmealtable.domain.member.MemberProfile;
import com.stcom.smartmealtable.domain.member.MemberType;
import com.stcom.smartmealtable.repository.AddressEntityRepository;
import com.stcom.smartmealtable.repository.GroupRepository;
import com.stcom.smartmealtable.repository.MemberProfileRepository;
import com.stcom.smartmealtable.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MemberProfileServiceCompleteIntegrationTest {

    @Autowired
    private MemberProfileService memberProfileService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberProfileRepository memberProfileRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private AddressEntityRepository addressEntityRepository;

    private Member member;
    private Group group;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .email("profile_complete_test@example.com")
                .rawPassword("password123!")
                .build();
        memberRepository.save(member);

        Address groupAddress = Address.builder()
                .lotNumberAddress("서울시 강남구")
                .roadAddress("테헤란로 123")
                .detailAddress("456번지")
                .build();
        group = new SchoolGroup(groupAddress, "테스트학교", SchoolType.UNIVERSITY_FOUR_YEAR);
        groupRepository.save(group);
    }

    @Test
    @DisplayName("새로운 프로필을 생성할 수 있다")
    void createProfile() {
        // given
        String nickName = "새프로필";
        MemberType type = MemberType.STUDENT;

        // when
        memberProfileService.createProfile(nickName, member.getId(), type, group.getId());

        // then
        MemberProfile created = memberProfileRepository.findAll().stream()
                .filter(p -> p.getMember().getId().equals(member.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(created.getNickName()).isEqualTo(nickName);
        assertThat(created.getType()).isEqualTo(type);
        assertThat(created.getGroup().getId()).isEqualTo(group.getId());
    }

    @Test
    @DisplayName("그룹 없이도 프로필을 생성할 수 있다")
    void createProfileWithoutGroup() {
        // given
        String nickName = "그룹없는프로필";
        MemberType type = MemberType.WORKER;

        // when
        memberProfileService.createProfile(nickName, member.getId(), type, null);

        // then
        MemberProfile created = memberProfileRepository.findAll().stream()
                .filter(p -> p.getMember().getId().equals(member.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(created.getNickName()).isEqualTo(nickName);
        assertThat(created.getType()).isEqualTo(type);
        assertThat(created.getGroup()).isNull();
    }

    @Test
    @DisplayName("존재하지 않는 회원으로 프로필을 생성하면 예외가 발생한다")
    void createProfileWithInvalidMemberId() {
        // given
        Long invalidMemberId = 99999L;

        // when & then
        assertThatThrownBy(() -> memberProfileService.createProfile("닉네임", invalidMemberId, MemberType.STUDENT, group.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 회원입니다");
    }

    @Test
    @DisplayName("새로운 주소를 저장할 수 있다")
    void saveNewAddress() {
        // given
        MemberProfile profile = createTestProfile();
        Address address = Address.builder()
                .lotNumberAddress("부산시 해운대구")
                .roadAddress("센텀로 100")
                .detailAddress("101동 102호")
                .build();
        String alias = "집";
        AddressType addressType = AddressType.HOME;

        // when
        memberProfileService.saveNewAddress(profile.getId(), address, alias, addressType);

        // then
        MemberProfile updatedProfile = memberProfileRepository.findById(profile.getId()).orElseThrow();
        assertThat(updatedProfile.getAddressHistory()).hasSize(1);
        
        AddressEntity savedAddress = updatedProfile.getAddressHistory().get(0);
        assertThat(savedAddress.getAddress().getLotNumberAddress()).isEqualTo("부산시 해운대구");
        assertThat(savedAddress.getAlias()).isEqualTo(alias);
        assertThat(savedAddress.getType()).isEqualTo(addressType);
    }

    @Test
    @DisplayName("주소를 기본 주소로 설정할 수 있다")
    void changeAddressToPrimary() {
        // given
        MemberProfile profile = createTestProfile();
        AddressEntity addressEntity = createTestAddress(profile);

        // when
        memberProfileService.changeAddressToPrimary(profile.getId(), addressEntity.getId());

        // then
        MemberProfile updatedProfile = memberProfileRepository.findById(profile.getId()).orElseThrow();
        assertThat(updatedProfile.findPrimaryAddress().getId()).isEqualTo(addressEntity.getId());
    }

    @Test
    @DisplayName("존재하지 않는 프로필로 주소 관련 작업을 하면 예외가 발생한다")
    void addressOperationsWithInvalidProfileId() {
        // given
        Long invalidProfileId = 99999L;
        Address address = Address.builder()
                .lotNumberAddress("테스트시")
                .roadAddress("테스트로")
                .detailAddress("테스트빌딩")
                .build();

        // when & then
        assertThatThrownBy(() -> memberProfileService.saveNewAddress(invalidProfileId, address, "별칭", AddressType.HOME))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 프로필입니다");

        assertThatThrownBy(() -> memberProfileService.changeAddressToPrimary(invalidProfileId, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 프로필입니다");
    }

    @Test
    @DisplayName("존재하지 않는 주소로 기본 주소 설정을 하면 예외가 발생한다")
    void changeAddressToPrimaryWithInvalidAddressId() {
        // given
        MemberProfile profile = createTestProfile();
        Long invalidAddressId = 99999L;

        // when & then
        assertThatThrownBy(() -> memberProfileService.changeAddressToPrimary(profile.getId(), invalidAddressId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 회원 주소 정보입니다.");
    }

    @Test
    @DisplayName("존재하지 않는 주소 엔티티로 주소 수정을 하면 예외가 발생한다")
    void changeAddressWithInvalidAddressEntityId() {
        // given
        MemberProfile profile = createTestProfile();
        Long invalidAddressEntityId = 99999L;
        Address newAddress = Address.builder()
                .lotNumberAddress("새주소시")
                .roadAddress("새주소로")
                .detailAddress("새주소빌딩")
                .build();

        // when & then
        assertThatThrownBy(() -> memberProfileService.changeAddress(
                profile.getId(), invalidAddressEntityId, newAddress, "새별칭", AddressType.OFFICE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 회원 주소 정보입니다.");
    }

    @Test
    @DisplayName("존재하지 않는 주소 엔티티로 주소 삭제를 하면 예외가 발생한다")
    void deleteAddressWithInvalidAddressEntityId() {
        // given
        MemberProfile profile = createTestProfile();
        Long invalidAddressEntityId = 99999L;

        // when & then
        assertThatThrownBy(() -> memberProfileService.deleteAddress(profile.getId(), invalidAddressEntityId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 회원 주소 정보입니다.");
    }

    @Test
    @DisplayName("프로필 조회 시 존재하지 않는 프로필 ID로 조회하면 예외가 발생한다")
    void getProfileFetchWithInvalidId() {
        // given
        Long invalidProfileId = 99999L;

        // when & then
        assertThatThrownBy(() -> memberProfileService.getProfileFetch(invalidProfileId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 프로필입니다");
    }

    @Test
    @DisplayName("프로필을 정상적으로 조회할 수 있다")
    void getProfileFetch() {
        // given
        MemberProfile profile = createTestProfile();

        // when
        MemberProfile retrieved = memberProfileService.getProfileFetch(profile.getId());

        // then
        assertThat(retrieved.getId()).isEqualTo(profile.getId());
        assertThat(retrieved.getNickName()).isEqualTo(profile.getNickName());
        assertThat(retrieved.getType()).isEqualTo(profile.getType());
    }

    @Test
    @DisplayName("프로필 변경 시 존재하지 않는 프로필 ID로 시도하면 예외가 발생한다")
    void changeProfileWithInvalidId() {
        // given
        Long invalidProfileId = 99999L;

        // when & then
        assertThatThrownBy(() -> memberProfileService.changeProfile(
                invalidProfileId, "새닉네임", MemberType.WORKER, group.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 프로필입니다");
    }

    private MemberProfile createTestProfile() {
        MemberProfile profile = MemberProfile.builder()
                .nickName("테스트프로필")
                .member(member)
                .type(MemberType.STUDENT)
                .group(group)
                .build();
        return memberProfileRepository.save(profile);
    }

    private AddressEntity createTestAddress(MemberProfile profile) {
        Address address = Address.builder()
                .lotNumberAddress("서울시")
                .roadAddress("테스트로")
                .detailAddress("테스트빌딩")
                .build();
        AddressEntity addressEntity = AddressEntity.builder()
                .address(address)
                .alias("테스트주소")
                .type(AddressType.HOME)
                .build();
        
        AddressEntity saved = addressEntityRepository.save(addressEntity);
        profile.addAddress(saved);
        memberProfileRepository.save(profile);
        return saved;
    }
} 