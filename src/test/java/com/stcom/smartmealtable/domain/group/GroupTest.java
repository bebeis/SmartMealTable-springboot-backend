package com.stcom.smartmealtable.domain.group;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.stcom.smartmealtable.domain.Address.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class GroupTest {

    @Test
    @DisplayName("CompanyGroup의 getTypeName은 industryType의 description을 반환한다")
    void companyGroupGetTypeName() {
        // given
        CompanyGroup companyGroup = new CompanyGroup();
        ReflectionTestUtils.setField(companyGroup, "industryType", IndustryType.IT);
        
        // when
        String typeName = companyGroup.getTypeName();
        
        // then
        assertThat(typeName).isEqualTo("IT");
    }
    
    @Test
    @DisplayName("SchoolGroup의 getTypeName은 schoolType의 name을 반환한다")
    void schoolGroupGetTypeName() {
        // given
        SchoolGroup schoolGroup = new SchoolGroup();
        ReflectionTestUtils.setField(schoolGroup, "schoolType", SchoolType.UNIVERSITY_FOUR_YEAR);
        
        // when
        String typeName = schoolGroup.getTypeName();
        
        // then
        assertThat(typeName).isEqualTo("UNIVERSITY_FOUR_YEAR");
    }
    
    @Test
    @DisplayName("IndustryType enum의 getDescription 메소드가 올바른 값을 반환한다")
    void industryTypeGetDescription() {
        // given & when & then
        assertThat(IndustryType.IT.getDescription()).isEqualTo("IT");
        assertThat(IndustryType.FINANCE.getDescription()).isEqualTo("파이낸스");
        assertThat(IndustryType.MANUFACTURING.getDescription()).isEqualTo("제조업");
        assertThat(IndustryType.SERVICE.getDescription()).isEqualTo("서비스");
    }
    
    @Test
    @DisplayName("SchoolType enum의 getDescription 메소드가 올바른 값을 반환한다")
    void schoolTypeGetDescription() {
        // given & when & then
        assertThat(SchoolType.UNIVERSITY_FOUR_YEAR.getDescription()).isEqualTo("대학교(4년제)");
        assertThat(SchoolType.UNIVERSITY_TWO_YEAR.getDescription()).isEqualTo("대학교(2년제)");
        assertThat(SchoolType.HIGH_SCHOOL.getDescription()).isEqualTo("고등학교");
        assertThat(SchoolType.MIDDLE_SCHOOL.getDescription()).isEqualTo("중학교");
    }
} 