package com.stcom.smartmealtable.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.stcom.smartmealtable.domain.Address.Address;
import com.stcom.smartmealtable.domain.group.CompanyGroup;
import com.stcom.smartmealtable.domain.group.Group;
import com.stcom.smartmealtable.domain.group.IndustryType;
import com.stcom.smartmealtable.domain.group.SchoolGroup;
import com.stcom.smartmealtable.domain.group.SchoolType;
import com.stcom.smartmealtable.repository.GroupRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * GroupService 통합 테스트.
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
class GroupServiceIntegrationTest {

    @Autowired
    private GroupService groupService;

    @Autowired
    private GroupRepository groupRepository;

    @Test
    @DisplayName("키워드로 그룹 검색이 가능해야 한다")
    void searchByKeyword() {
        // given
        Address address1 = Address.builder()
                .roadAddress("서울특별시 종로구 세종대로 1")
                .detailAddress("본관")
                .build();
        Group group1 = new SchoolGroup(address1, "서울고등학교", SchoolType.HIGH_SCHOOL);
        groupRepository.save(group1);

        Address address2 = Address.builder()
                .roadAddress("서울특별시 서초구 강남대로 1")
                .detailAddress("타워")
                .build();
        Group group2 = new CompanyGroup();
        org.springframework.test.util.ReflectionTestUtils.setField(group2, "address", address2);
        org.springframework.test.util.ReflectionTestUtils.setField(group2, "name", "테스트IT");
        org.springframework.test.util.ReflectionTestUtils.setField(group2, "industryType", IndustryType.IT);
        groupRepository.save(group2);

        // when
        List<Group> result = groupService.findGroupsByKeyword("서울");

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("서울고등학교");
    }
} 