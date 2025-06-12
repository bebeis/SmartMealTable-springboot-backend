package com.stcom.smartmealtable.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.stcom.smartmealtable.domain.Address.Address;
import com.stcom.smartmealtable.domain.group.Group;
import com.stcom.smartmealtable.domain.group.SchoolType;
import com.stcom.smartmealtable.infrastructure.KakaoAddressApiService;
import com.stcom.smartmealtable.infrastructure.dto.AddressRequest;
import com.stcom.smartmealtable.repository.GroupRepository;
import java.util.List;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(GroupServiceAdditionalIntegrationTest.FakeKakaoConfig.class)
class GroupServiceAdditionalIntegrationTest {

    @TestConfiguration
    static class FakeKakaoConfig {
        @Bean
        KakaoAddressApiService kakaoAddressApiService() {
            return new KakaoAddressApiService() {
                @Override
                public Address createAddressFromRequest(AddressRequest requestDto) {
                    return Address.builder()
                            .roadAddress(requestDto.getRoadAddress())
                            .detailAddress(requestDto.getDetailAddress())
                            .build();
                }
            };
        }
    }

    @Autowired
    private GroupService groupService;
    @Autowired
    private GroupRepository repository;

    @Test
    @DisplayName("학교 그룹을 생성하고 삭제할 수 있다")
    @Rollback
    void createAndDeleteSchoolGroup() {
        // when
        groupService.createSchoolGroup(new AddressRequest("서울", "1"), "테스트고", SchoolType.HIGH_SCHOOL);
        List<Group> saved = repository.findByNameContaining("테스트고", org.springframework.data.domain.Limit.of(10));
        Long id = saved.get(0).getId();

        // then
        assertThat(saved).hasSize(1);

        groupService.deleteGroup(id);
        assertThat(repository.findById(id)).isEmpty();
    }

    @Test
    @DisplayName("학교 그룹 수정 시 타입 변경")
    void changeSchoolGroup() {
        groupService.createSchoolGroup(new AddressRequest("부산", "detail"), "부산고", SchoolType.HIGH_SCHOOL);
        Long id = repository.findByNameContaining("부산고", org.springframework.data.domain.Limit.of(10)).get(0).getId();

        groupService.changeSchoolGroup(id, new AddressRequest("부산", "detail2"), "부산여고", SchoolType.MIDDLE_SCHOOL);

        Group changed = repository.findById(id).orElseThrow();
        assertThat(changed.getName()).isEqualTo("부산여고");
        assertThat(changed.getTypeName()).isEqualTo(SchoolType.MIDDLE_SCHOOL.name());
    }

    @Test
    @DisplayName("학교 그룹 수정 시 학교 그룹이 아니면 예외")
    void changeSchoolGroup_invalidType() {
        // 직접 회사 그룹 저장
        Address addr = Address.builder().roadAddress("서울").build();
        Group company = new com.stcom.smartmealtable.domain.group.CompanyGroup();
        org.springframework.test.util.ReflectionTestUtils.setField(company, "address", addr);
        org.springframework.test.util.ReflectionTestUtils.setField(company, "name", "컴퍼니");
        repository.save(company);

        assertThatThrownBy(() ->
                groupService.changeSchoolGroup(company.getId(), new AddressRequest("a","b"), "x", SchoolType.HIGH_SCHOOL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("학교 그룹이 아닙니다");
    }
} 