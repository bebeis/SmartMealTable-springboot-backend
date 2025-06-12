package com.stcom.smartmealtable.domain.group;

import static org.assertj.core.api.Assertions.assertThat;

import com.stcom.smartmealtable.domain.Address.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 순수 자바 단위 테스트: 엔티티의 비즈니스 로직 검증.
 */
class SchoolGroupTest {

    @Test
    @DisplayName("학교 그룹 타입과 주소/이름 변경이 정상동작해야 한다")
    void changeFields() {
        // given
        Address address = Address.builder()
                .roadAddress("서울시 종로구 세종대로 1")
                .detailAddress("본관 1층")
                .build();
        SchoolGroup group = new SchoolGroup(address, "서울대학교", SchoolType.UNIVERSITY_FOUR_YEAR);

        // when
        Address newAddress = Address.builder()
                .roadAddress("서울시 관악구 관악로 1")
                .detailAddress("행정관 2층")
                .build();
        group.changeNameAndAddress("국민대학교", newAddress);
        group.changeType(SchoolType.HIGH_SCHOOL);

        // then
        assertThat(group.getName()).isEqualTo("국민대학교");
        assertThat(group.getAddress().getRoadAddress()).isEqualTo("서울시 관악구 관악로 1");
        assertThat(group.getTypeName()).isEqualTo(SchoolType.HIGH_SCHOOL.name());
    }
} 