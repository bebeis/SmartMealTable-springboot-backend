package com.stcom.smartmealtable.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stcom.smartmealtable.domain.Address.Address;
import com.stcom.smartmealtable.domain.group.CompanyGroup;
import com.stcom.smartmealtable.domain.group.Group;
import com.stcom.smartmealtable.domain.group.IndustryType;
import com.stcom.smartmealtable.domain.group.SchoolGroup;
import com.stcom.smartmealtable.domain.group.SchoolType;
import com.stcom.smartmealtable.repository.GroupRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Limit;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @InjectMocks
    private GroupServiceImpl groupService;

    @Test
    @DisplayName("ID로 그룹을 찾을 수 있어야 한다")
    void findGroupByGroupId() {
        // given
        Long groupId = 1L;
        
        CompanyGroup companyGroup = new CompanyGroup();
        Address address = createAddress("서울시 강남구 테헤란로 123");
        
        // 리플렉션으로 private 필드 설정
        org.springframework.test.util.ReflectionTestUtils.setField(companyGroup, "id", groupId);
        org.springframework.test.util.ReflectionTestUtils.setField(companyGroup, "name", "IT 회사");
        org.springframework.test.util.ReflectionTestUtils.setField(companyGroup, "address", address);
        org.springframework.test.util.ReflectionTestUtils.setField(companyGroup, "industryType", IndustryType.IT);

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(companyGroup));

        // when
        Group foundGroup = groupService.findGroupByGroupId(groupId);

        // then
        assertThat(foundGroup).isEqualTo(companyGroup);
        assertThat(foundGroup.getName()).isEqualTo("IT 회사");
        assertThat(foundGroup.getTypeName()).isEqualTo("IT");
        assertThat(foundGroup.getAddress().getRoadAddress()).isEqualTo("서울시 강남구 테헤란로 123");
        verify(groupRepository, times(1)).findById(groupId);
    }

    @Test
    @DisplayName("존재하지 않는 그룹 ID로 조회 시 예외가 발생해야 한다")
    void findGroupByGroupIdNotFound() {
        // given
        Long groupId = 999L;
        when(groupRepository.findById(groupId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> groupService.findGroupByGroupId(groupId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 회원입니다");
    }

    @Test
    @DisplayName("키워드로 그룹을 검색할 수 있어야 한다")
    void findGroupsByKeyword() {
        // given
        String keyword = "학교";
        
        SchoolGroup schoolGroup1 = createSchoolGroup(1L, "서울대학교", SchoolType.UNIVERSITY_FOUR_YEAR, 
                createAddress("서울시 관악구 관악로 1"));
        
        SchoolGroup schoolGroup2 = createSchoolGroup(2L, "부산대학교", SchoolType.UNIVERSITY_FOUR_YEAR,
                createAddress("부산시 금정구 부산대학로 63번길 2"));
        
        List<Group> expectedGroups = Arrays.asList(schoolGroup1, schoolGroup2);
        
        when(groupRepository.findByNameContaining(keyword, Limit.of(10))).thenReturn(expectedGroups);

        // when
        List<Group> foundGroups = groupService.findGroupsByKeyword(keyword);

        // then
        assertThat(foundGroups).hasSize(2);
        assertThat(foundGroups).containsExactly(schoolGroup1, schoolGroup2);
        
        assertThat(foundGroups.get(0).getName()).isEqualTo("서울대학교");
        assertThat(foundGroups.get(0).getTypeName()).isEqualTo("UNIVERSITY_FOUR_YEAR");
        
        assertThat(foundGroups.get(1).getName()).isEqualTo("부산대학교");
        assertThat(foundGroups.get(1).getAddress().getRoadAddress()).isEqualTo("부산시 금정구 부산대학로 63번길 2");
        
        verify(groupRepository, times(1)).findByNameContaining(keyword, Limit.of(10));
    }
    
    @Test
    @DisplayName("키워드 검색 결과가 없을 경우 빈 리스트를 반환해야 한다")
    void findGroupsByKeywordNoResult() {
        // given
        String keyword = "존재하지 않는 키워드";
        when(groupRepository.findByNameContaining(keyword, Limit.of(10))).thenReturn(List.of());

        // when
        List<Group> foundGroups = groupService.findGroupsByKeyword(keyword);

        // then
        assertThat(foundGroups).isEmpty();
        verify(groupRepository, times(1)).findByNameContaining(keyword, Limit.of(10));
    }
    
    // 테스트용 주소 생성 헬퍼 메소드
    private Address createAddress(String roadAddress) {
        Address address = new Address();
        org.springframework.test.util.ReflectionTestUtils.setField(address, "roadAddress", roadAddress);
        return address;
    }
    
    // 테스트용 학교 그룹 생성 헬퍼 메소드
    private SchoolGroup createSchoolGroup(Long id, String name, SchoolType schoolType, Address address) {
        SchoolGroup group = new SchoolGroup();
        org.springframework.test.util.ReflectionTestUtils.setField(group, "id", id);
        org.springframework.test.util.ReflectionTestUtils.setField(group, "name", name);
        org.springframework.test.util.ReflectionTestUtils.setField(group, "address", address);
        org.springframework.test.util.ReflectionTestUtils.setField(group, "schoolType", schoolType);
        return group;
    }
} 