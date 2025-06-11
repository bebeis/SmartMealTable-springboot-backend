package com.stcom.smartmealtable.domain.group;

import static org.assertj.core.api.Assertions.assertThat;

import com.stcom.smartmealtable.domain.Address.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CompanyGroupTest {

    @Test
    @DisplayName("회사 그룹의 타입명(산업군)과 주소 변경이 정상 동작해야 한다")
    void changeFields() {
        // given
        Address address = Address.builder()
                .roadAddress("서울특별시 강남구 테헤란로 1")
                .detailAddress("10층")
                .build();
        CompanyGroup group = new CompanyGroup();
        org.springframework.test.util.ReflectionTestUtils.setField(group, "address", address);
        org.springframework.test.util.ReflectionTestUtils.setField(group, "name", "테스트IT");
        org.springframework.test.util.ReflectionTestUtils.setField(group, "industryType", IndustryType.IT);

        // when
        Address newAddress = Address.builder()
                .roadAddress("서울특별시 영등포구 국제금융로 2")
                .detailAddress("6층")
                .build();
        group.changeNameAndAddress("테스트금융", newAddress);
        org.springframework.test.util.ReflectionTestUtils.setField(group, "industryType", IndustryType.FINANCE);

        // then
        assertThat(group.getName()).isEqualTo("테스트금융");
        assertThat(group.getAddress().getRoadAddress()).isEqualTo("서울특별시 영등포구 국제금융로 2");
        assertThat(group.getTypeName()).isEqualTo("FINANCE");
    }
} 