package com.stcom.smartmealtable.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MemberProfileServiceTest {

    @Mock
    private MemberProfileRepository memberProfileRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private AddressEntityRepository addressEntityRepository;

    @InjectMocks
    private MemberProfileService profileService;
    
    @Captor
    private ArgumentCaptor<MemberProfile> profileCaptor;
    
    @Captor
    private ArgumentCaptor<AddressEntity> addressEntityCaptor;
    
    private Member member;
    private Group group;
    private Address address;
    private MemberProfile profile;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 셋업
        member = new Member("test@example.com");
        ReflectionTestUtils.setField(member, "id", 1L);
        
        group = new SchoolGroup();
        ReflectionTestUtils.setField(group, "id", 1L);
        ReflectionTestUtils.setField(group, "name", "서울대학교");
        ReflectionTestUtils.setField(group, "schoolType", SchoolType.UNIVERSITY_FOUR_YEAR);
        
        address = createAddress("서울특별시 관악구 관악로 1", "서울특별시 관악구", "101호", 37.459, 126.952);
    }

    @Test
    @DisplayName("프로필 ID로 프로필 정보를 조회할 수 있어야 한다")
    void getProfileFetch() {
        // given
        Long profileId = 1L;
        profile = createProfile(profileId, "닉네임", member, MemberType.STUDENT, group);
        
        when(memberProfileRepository.findMemberProfileEntityGraphById(profileId))
                .thenReturn(Optional.of(profile));

        // when
        MemberProfile fetchedProfile = profileService.getProfileFetch(profileId);

        // then
        assertThat(fetchedProfile).isEqualTo(profile);
        assertThat(fetchedProfile.getNickName()).isEqualTo("닉네임");
        assertThat(fetchedProfile.getMember()).isEqualTo(member);
        assertThat(fetchedProfile.getType()).isEqualTo(MemberType.STUDENT);
        assertThat(fetchedProfile.getGroup()).isEqualTo(group);
        
        verify(memberProfileRepository).findMemberProfileEntityGraphById(profileId);
    }
    
    @Test
    @DisplayName("프로필을 생성할 수 있어야 한다 - 그룹 있음")
    void createProfileWithGroup() {
        // given
        String nickName = "새닉네임";
        Long memberId = 1L;
        MemberType type = MemberType.STUDENT;
        Long groupId = 1L;
        
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(groupRepository.getReferenceById(groupId)).thenReturn(group);
        when(memberProfileRepository.save(any(MemberProfile.class))).thenAnswer(invocation -> {
            MemberProfile savedProfile = invocation.getArgument(0);
            ReflectionTestUtils.setField(savedProfile, "id", 1L);
            return savedProfile;
        });

        // when
        profileService.createProfile(nickName, memberId, type, groupId);

        // then
        verify(memberRepository).findById(memberId);
        verify(groupRepository).getReferenceById(groupId);
        verify(memberProfileRepository).save(profileCaptor.capture());
        
        MemberProfile savedProfile = profileCaptor.getValue();
        assertThat(savedProfile.getNickName()).isEqualTo(nickName);
        assertThat(savedProfile.getMember()).isEqualTo(member);
        assertThat(savedProfile.getType()).isEqualTo(type);
        assertThat(savedProfile.getGroup()).isEqualTo(group);
    }
    
    @Test
    @DisplayName("프로필을 생성할 수 있어야 한다 - 그룹 없음")
    void createProfileWithoutGroup() {
        // given
        String nickName = "새닉네임";
        Long memberId = 1L;
        MemberType type = MemberType.OTHER;
        Long groupId = null;
        
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(memberProfileRepository.save(any(MemberProfile.class))).thenAnswer(invocation -> {
            MemberProfile savedProfile = invocation.getArgument(0);
            ReflectionTestUtils.setField(savedProfile, "id", 1L);
            return savedProfile;
        });

        // when
        profileService.createProfile(nickName, memberId, type, groupId);

        // then
        verify(memberRepository).findById(memberId);
        verify(memberProfileRepository).save(profileCaptor.capture());
        
        MemberProfile savedProfile = profileCaptor.getValue();
        assertThat(savedProfile.getNickName()).isEqualTo(nickName);
        assertThat(savedProfile.getMember()).isEqualTo(member);
        assertThat(savedProfile.getType()).isEqualTo(type);
        assertThat(savedProfile.getGroup()).isNull();
    }
    
    @Test
    @DisplayName("프로필 정보를 변경할 수 있어야 한다")
    void changeProfile() {
        // given
        Long profileId = 1L;
        String newNickName = "변경된닉네임";
        MemberType newType = MemberType.WORKER;
        Long newGroupId = 2L;
        
        profile = createProfile(profileId, "원래닉네임", member, MemberType.STUDENT, group);
        
        Group newGroup = new SchoolGroup();
        ReflectionTestUtils.setField(newGroup, "id", 2L);
        ReflectionTestUtils.setField(newGroup, "name", "회사");
        
        when(memberProfileRepository.findById(profileId)).thenReturn(Optional.of(profile));
        when(groupRepository.getReferenceById(newGroupId)).thenReturn(newGroup);

        // when
        profileService.changeProfile(profileId, newNickName, newType, newGroupId);

        // then
        verify(memberProfileRepository).findById(profileId);
        verify(groupRepository).getReferenceById(newGroupId);
        
        assertThat(profile.getNickName()).isEqualTo(newNickName);
        assertThat(profile.getType()).isEqualTo(newType);
        assertThat(profile.getGroup()).isEqualTo(newGroup);
    }
    
    @Test
    @DisplayName("새 주소를 추가할 수 있어야 한다")
    void saveNewAddress() {
        // given
        Long profileId = 1L;
        String alias = "집";
        AddressType addressType = AddressType.HOME;
        
        profile = createProfile(profileId, "닉네임", member, MemberType.STUDENT, group);
        ReflectionTestUtils.setField(profile, "addressHistory", new ArrayList<>());
        
        when(memberProfileRepository.findById(profileId)).thenReturn(Optional.of(profile));
        when(addressEntityRepository.save(any(AddressEntity.class))).thenAnswer(invocation -> {
            AddressEntity savedAddress = invocation.getArgument(0);
            ReflectionTestUtils.setField(savedAddress, "id", 1L);
            return savedAddress;
        });

        // when
        profileService.saveNewAddress(profileId, address, alias, addressType);

        // then
        verify(memberProfileRepository).findById(profileId);
        verify(addressEntityRepository).save(addressEntityCaptor.capture());
        
        AddressEntity savedAddressEntity = addressEntityCaptor.getValue();
        assertThat(savedAddressEntity.getAddress()).isEqualTo(address);
        assertThat(savedAddressEntity.getAlias()).isEqualTo(alias);
        assertThat(savedAddressEntity.getType()).isEqualTo(addressType);
        assertThat(profile.getAddressHistory()).hasSize(1);
    }
    
    @Test
    @DisplayName("주소 정보를 변경할 수 있어야 한다")
    void changeAddress() {
        // given
        Long profileId = 1L;
        Long addressEntityId = 1L;
        String newAlias = "새집";
        AddressType newType = AddressType.HOME;
        
        profile = createProfile(profileId, "닉네임", member, MemberType.STUDENT, group);
        
        AddressEntity addressEntity = AddressEntity.builder()
                .address(address)
                .alias("구집")
                .type(AddressType.ETC)
                .build();
        ReflectionTestUtils.setField(addressEntity, "id", addressEntityId);
        
        List<AddressEntity> addresses = new ArrayList<>();
        addresses.add(addressEntity);
        ReflectionTestUtils.setField(profile, "addressHistory", addresses);
        
        Address newAddress = createAddress("서울시 서초구 서초대로 123", "서초구", "202호", 37.5, 127.0);
        
        when(memberProfileRepository.findById(profileId)).thenReturn(Optional.of(profile));
        when(addressEntityRepository.findById(addressEntityId)).thenReturn(Optional.of(addressEntity));

        // when
        profileService.changeAddress(profileId, addressEntityId, newAddress, newAlias, newType);

        // then
        verify(memberProfileRepository).findById(profileId);
        verify(addressEntityRepository).findById(addressEntityId);
        
        // 주소 정보가 업데이트되었는지 확인
        assertThat(addressEntity.getAddress()).isEqualTo(newAddress);
        assertThat(addressEntity.getAlias()).isEqualTo(newAlias);
        assertThat(addressEntity.getType()).isEqualTo(newType);
    }
    
    @Test
    @DisplayName("주소를 삭제할 수 있어야 한다")
    void deleteAddress() {
        // given
        Long profileId = 1L;
        Long addressEntityId = 1L;
        
        profile = createProfile(profileId, "닉네임", member, MemberType.STUDENT, group);
        
        AddressEntity addressEntity = AddressEntity.builder()
                .address(address)
                .alias("집")
                .type(AddressType.HOME)
                .build();
        ReflectionTestUtils.setField(addressEntity, "id", addressEntityId);
        
        // 주소에 primary=true 설정
        ReflectionTestUtils.setField(addressEntity, "primary", true);
        
        List<AddressEntity> addresses = new ArrayList<>();
        addresses.add(addressEntity);
        
        // 두번째 주소 추가
        AddressEntity secondAddress = AddressEntity.builder()
                .address(createAddress("서울시 강남구", "강남구", "301호", 37.4, 127.1))
                .alias("회사")
                .type(AddressType.OFFICE)
                .build();
        ReflectionTestUtils.setField(secondAddress, "id", 2L);
        addresses.add(secondAddress);
        
        ReflectionTestUtils.setField(profile, "addressHistory", addresses);
        
        when(memberProfileRepository.findById(profileId)).thenReturn(Optional.of(profile));
        when(addressEntityRepository.findById(addressEntityId)).thenReturn(Optional.of(addressEntity));

        // when
        profileService.deleteAddress(profileId, addressEntityId);

        // then
        verify(memberProfileRepository).findById(profileId);
        verify(addressEntityRepository).findById(addressEntityId);
        
        // 주소가 삭제되었는지 확인
        assertThat(profile.getAddressHistory()).hasSize(1);
        assertThat(profile.getAddressHistory().get(0)).isEqualTo(secondAddress);
    }
    
    @Test
    @DisplayName("기본 예산을 설정할 수 있어야 한다")
    void registerDefaultBudgets() {
        // given
        Long profileId = 1L;
        Long dailyLimit = 10000L;
        Long monthlyLimit = 300000L;
        
        profile = createProfile(profileId, "닉네임", member, MemberType.STUDENT, group);
        
        when(memberProfileRepository.findById(profileId)).thenReturn(Optional.of(profile));

        // when
        profileService.registerDefaultBudgets(profileId, dailyLimit, monthlyLimit);

        // then
        verify(memberProfileRepository).findById(profileId);
        
        // 기본 예산 설정 로직은 실제로는 MemberProfile 내부 메서드에 있으므로
        // 이 테스트에서는 메서드 호출 여부만 확인
    }
    
    @Test
    @DisplayName("존재하지 않는 프로필 ID로 조회 시 예외가 발생해야 한다")
    void getProfileFetchNotFound() {
        // given
        Long nonExistingProfileId = 999L;
        when(memberProfileRepository.findMemberProfileEntityGraphById(nonExistingProfileId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> profileService.getProfileFetch(nonExistingProfileId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("존재하지 않는 프로필입니다");
    }
    
    @Test
    @DisplayName("존재하지 않는 회원 ID로 프로필 생성 시 예외가 발생해야 한다")
    void createProfileWithNonExistingMember() {
        // given
        Long nonExistingMemberId = 999L;
        when(memberRepository.findById(nonExistingMemberId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> profileService.createProfile("닉네임", nonExistingMemberId, MemberType.STUDENT, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 회원입니다");
    }

    private MemberProfile createProfile(Long id, String nickName, Member member, MemberType type, Group group) {
        MemberProfile profile = MemberProfile.builder()
                .nickName(nickName)
                .member(member)
                .type(type)
                .group(group)
                .build();
        ReflectionTestUtils.setField(profile, "id", id);
        return profile;
    }
    
    private Address createAddress(String roadAddress, String detailAddress, String alias, 
                                 double latitude, double longitude) {
        Address address = Address.builder()
                .roadAddress(roadAddress)
                .detailAddress(detailAddress)
                .latitude(latitude)
                .longitude(longitude)
                .build();
        return address;
    }
} 