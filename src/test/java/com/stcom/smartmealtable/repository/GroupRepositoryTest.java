package com.stcom.smartmealtable.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.stcom.smartmealtable.domain.Address.Address;
import com.stcom.smartmealtable.domain.group.SchoolGroup;
import com.stcom.smartmealtable.domain.group.SchoolType;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Limit;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class GroupRepositoryTest {

    @Autowired
    private GroupRepository groupRepository;

    @Test
    @DisplayName("이름 키워드로 그룹을 조회할 수 있어야 한다")
    void findByNameContaining() {
        // given
        Address address = Address.builder()
                .roadAddress("서울특별시 강남구 테헤란로 123")
                .detailAddress("7층")
                .build();
        SchoolGroup saved = groupRepository.save(new SchoolGroup(address, "테스트고등학교", SchoolType.HIGH_SCHOOL));

        // when
        List<com.stcom.smartmealtable.domain.group.Group> result =
                groupRepository.findByNameContaining("테스트", Limit.of(10));

        // then
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getId()).isEqualTo(saved.getId());
    }
} 