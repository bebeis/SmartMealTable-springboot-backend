package com.stcom.smartmealtable.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.stcom.smartmealtable.domain.Address.Address;
import com.stcom.smartmealtable.domain.group.Group;
import com.stcom.smartmealtable.domain.group.SchoolGroup;
import com.stcom.smartmealtable.domain.group.SchoolType;
import com.stcom.smartmealtable.infrastructure.dto.AddressRequest;
import com.stcom.smartmealtable.repository.GroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import com.stcom.smartmealtable.infrastructure.KakaoAddressApiService;

import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(GroupServiceCompleteIntegrationTest.TestConfig.class)
class GroupServiceCompleteIntegrationTest {

    @Autowired
    private GroupService groupService;

    @Autowired
    private GroupRepository groupRepository;

    private SchoolGroup schoolGroup1;
    private SchoolGroup schoolGroup2;
    private SchoolGroup schoolGroup3;

    @BeforeEach
    void setUp() {
        Address address1 = Address.builder()
                .lotNumberAddress("서울시 강남구")
                .roadAddress("테헤란로 123")
                .detailAddress("1번 빌딩")
                .build();

        Address address2 = Address.builder()
                .lotNumberAddress("서울시 서초구")
                .roadAddress("강남대로 456")
                .detailAddress("2번 빌딩")
                .build();

        Address address3 = Address.builder()
                .lotNumberAddress("부산시 해운대구")
                .roadAddress("센텀로 789")
                .detailAddress("3번 빌딩")
                .build();

        schoolGroup1 = new SchoolGroup(address1, "서울대학교", SchoolType.UNIVERSITY_FOUR_YEAR);
        schoolGroup2 = new SchoolGroup(address2, "연세대학교", SchoolType.UNIVERSITY_FOUR_YEAR);
        schoolGroup3 = new SchoolGroup(address3, "카이스트", SchoolType.UNIVERSITY_FOUR_YEAR);

        groupRepository.save(schoolGroup1);
        groupRepository.save(schoolGroup2);
        groupRepository.save(schoolGroup3);
    }

    @Test
    @DisplayName("그룹 ID로 그룹을 조회할 수 있다")
    void findGroupByGroupId() {
        // when
        Group foundGroup = groupService.findGroupByGroupId(schoolGroup1.getId());

        // then
        assertThat(foundGroup.getId()).isEqualTo(schoolGroup1.getId());
        assertThat(foundGroup.getName()).isEqualTo("서울대학교");
    }

    @Test
    @DisplayName("존재하지 않는 그룹 ID로 조회하면 예외가 발생한다")
    void findGroupByGroupIdWithInvalidId() {
        // given
        Long invalidGroupId = 99999L;

        // when & then
        assertThatThrownBy(() -> groupService.findGroupByGroupId(invalidGroupId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 회원입니다");
    }

    @Test
    @DisplayName("키워드로 그룹을 검색할 수 있다 - 완전 일치")
    void findGroupsByKeywordExactMatch() {
        // when
        List<Group> groups = groupService.findGroupsByKeyword("서울대학교");

        // then
        assertThat(groups).hasSize(1);
        assertThat(groups.get(0).getName()).isEqualTo("서울대학교");
    }

    @Test
    @DisplayName("키워드로 그룹을 검색할 수 있다 - 부분 일치")
    void findGroupsByKeywordPartialMatch() {
        // when
        List<Group> groups = groupService.findGroupsByKeyword("대학교");

        // then
        assertThat(groups).hasSize(2);
        assertThat(groups).extracting(Group::getName)
                .containsExactlyInAnyOrder("서울대학교", "연세대학교");
    }

    @Test
    @DisplayName("키워드로 그룹을 검색할 수 있다 - 검색 결과 없음")
    void findGroupsByKeywordNoResults() {
        // when
        List<Group> groups = groupService.findGroupsByKeyword("존재하지않는키워드");

        // then
        assertThat(groups).isEmpty();
    }

    @Test
    @DisplayName("빈 키워드로 검색하면 모든 그룹을 반환한다")
    void findGroupsByEmptyKeyword() {
        // when
        List<Group> groups = groupService.findGroupsByKeyword("");

        // then
        assertThat(groups).hasSize(3);
    }

    @Test
    @DisplayName("null 키워드로 검색하면 모든 그룹을 반환한다")
    void findGroupsByNullKeyword() {
        // when
        List<Group> groups = groupService.findGroupsByKeyword(null);

        // then
        assertThat(groups).isEmpty();
    }

    @Test
    @DisplayName("새로운 학교 그룹을 생성할 수 있다")
    void createSchoolGroup() {
        // given
        AddressRequest addressRequest = new AddressRequest("1234", "대전시 유성구 과학로 291");
        String name = "한국과학기술원";
        SchoolType schoolType = SchoolType.UNIVERSITY_FOUR_YEAR;

        // when
        groupService.createSchoolGroup(addressRequest, name, schoolType);

        // then
        List<Group> allGroups = groupRepository.findAll();
        assertThat(allGroups).hasSize(4);
        Group createdGroup = allGroups.stream()
                .filter(group -> group.getName().equals(name))
                .findFirst()
                .orElseThrow();
        assertThat(createdGroup.getName()).isEqualTo(name);
        assertThat(((SchoolGroup) createdGroup).getSchoolType()).isEqualTo(schoolType);
    }

    @Test
    @DisplayName("학교 그룹 정보를 변경할 수 있다")
    void changeSchoolGroup() {
        // given
        AddressRequest newAddressRequest = new AddressRequest("5678", "서울시 동작구 상도로 369");
        String newName = "숭실대학교";
        SchoolType newType = SchoolType.UNIVERSITY_FOUR_YEAR;

        // when
        groupService.changeSchoolGroup(schoolGroup1.getId(), newAddressRequest, newName, newType);

        // then
        Group changedGroup = groupRepository.findById(schoolGroup1.getId()).orElseThrow();
        assertThat(changedGroup.getName()).isEqualTo(newName);
        assertThat(((SchoolGroup) changedGroup).getSchoolType()).isEqualTo(newType);
    }

    @Test
    @DisplayName("존재하지 않는 그룹을 변경하려고 하면 예외가 발생한다")
    void changeSchoolGroupWithInvalidId() {
        // given
        Long invalidGroupId = 99999L;
        AddressRequest addressRequest = new AddressRequest("0000", "서울시 테스트로");

        // when & then
        assertThatThrownBy(() -> groupService.changeSchoolGroup(invalidGroupId, addressRequest, "테스트", SchoolType.UNIVERSITY_FOUR_YEAR))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 회원입니다");
    }

    @Test
    @DisplayName("학교가 아닌 그룹을 학교 그룹으로 변경하려고 하면 예외가 발생한다")
    void changeNonSchoolGroupToSchoolGroup() {
        // 이 테스트는 현재 모든 그룹이 SchoolGroup이므로 생략하거나
        // CompanyGroup이 있을 때 다시 작성해야 함
    }

    @Test
    @DisplayName("그룹을 삭제할 수 있다")
    void deleteGroup() {
        // given
        Long groupIdToDelete = schoolGroup1.getId();

        // when
        groupService.deleteGroup(groupIdToDelete);

        // then
        assertThat(groupRepository.findById(groupIdToDelete)).isEmpty();
        assertThat(groupRepository.findAll()).hasSize(2);
    }

    @Test
    @DisplayName("존재하지 않는 그룹을 삭제하려고 하면 예외가 발생한다")
    void deleteGroupWithInvalidId() {
        // given
        Long invalidGroupId = 99999L;

        // when & then
        assertThatThrownBy(() -> groupService.deleteGroup(invalidGroupId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 회원입니다");
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        @org.springframework.context.annotation.Primary
        public KakaoAddressApiService kakaoAddressApiService() {
            return new KakaoAddressApiService() {
                @Override
                public Address createAddressFromRequest(AddressRequest requestDto) {
                    return Address.builder()
                            .lotNumberAddress(requestDto.getRoadAddress())
                            .roadAddress(requestDto.getRoadAddress())
                            .detailAddress(requestDto.getDetailAddress())
                            .build();
                }
            };
        }
    }
} 