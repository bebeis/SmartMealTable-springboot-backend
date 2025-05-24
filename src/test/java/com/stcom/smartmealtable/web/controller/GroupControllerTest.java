package com.stcom.smartmealtable.web.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stcom.smartmealtable.domain.Address.Address;
import com.stcom.smartmealtable.domain.group.CompanyGroup;
import com.stcom.smartmealtable.domain.group.Group;
import com.stcom.smartmealtable.domain.group.IndustryType;
import com.stcom.smartmealtable.domain.group.SchoolGroup;
import com.stcom.smartmealtable.domain.group.SchoolType;
import com.stcom.smartmealtable.security.JwtTokenService;
import com.stcom.smartmealtable.service.GroupService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import io.jsonwebtoken.Claims;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = {
        "spring.main.allow-bean-definition-overriding=true"
})
class GroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GroupService groupService;
    
    @MockBean
    private JwtTokenService jwtTokenService;
    
    @Autowired
    private WebApplicationContext context;
    
    @BeforeEach
    void setup() {
        // 테스트용 MockMvc 설정
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
                .build();
                
        // UserContext 어노테이션 처리를 위한 설정
        // Claims 모킹 설정
        Claims claims = Mockito.mock(Claims.class);
        when(claims.get("memberId", String.class)).thenReturn("1");
        when(jwtTokenService.extractClaims(Mockito.anyString())).thenReturn(claims);
    }

    @Test
    @DisplayName("키워드로 그룹을 검색할 수 있다")
    void searchGroup() throws Exception {
        // given
        String keyword = "테스트";
        
        // 테스트용 그룹 생성
        CompanyGroup companyGroup = createCompanyGroup("테스트 회사", IndustryType.IT, 
                createAddress("서울시 강남구 테헤란로 123"));
                
        SchoolGroup schoolGroup = createSchoolGroup("테스트 학교", SchoolType.UNIVERSITY_FOUR_YEAR, 
                createAddress("서울시 서초구 방배로 456"));
        
        when(groupService.findGroupsByKeyword(keyword))
                .thenReturn(List.of(companyGroup, schoolGroup));

        // when & then
        mockMvc.perform(get("/api/v1/groups")
                        .param("keyword", keyword)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].name").value("테스트 회사"))
                .andExpect(jsonPath("$.data[0].groupType").value("IT"))
                .andExpect(jsonPath("$.data[0].roadAddress").value("서울시 강남구 테헤란로 123"))
                .andExpect(jsonPath("$.data[1].name").value("테스트 학교"))
                .andExpect(jsonPath("$.data[1].groupType").value("UNIVERSITY_FOUR_YEAR"))
                .andExpect(jsonPath("$.data[1].roadAddress").value("서울시 서초구 방배로 456"));
    }

    @Test
    @DisplayName("빈 키워드로 그룹 검색시 에러가 발생한다")
    void searchGroup_EmptyKeyword() throws Exception {
        // given
        String keyword = "";

        // when & then
        mockMvc.perform(get("/api/v1/groups")
                        .param("keyword", keyword)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value("키워드가 비어있습니다. 키워드를 입력해주세요"));
    }

    @Test
    @DisplayName("키워드로 그룹 검색시 결과가 없으면 빈 리스트를 반환한다")
    void searchGroup_NoResults() throws Exception {
        // given
        String keyword = "존재하지 않는 키워드";
        
        when(groupService.findGroupsByKeyword(keyword))
                .thenReturn(List.of());

        // when & then
        mockMvc.perform(get("/api/v1/groups")
                        .param("keyword", keyword)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }
    
    // 테스트용 주소 생성 헬퍼 메소드
    private Address createAddress(String roadAddress) {
        Address address = mock(Address.class);
        when(address.getRoadAddress()).thenReturn(roadAddress);
        return address;
    }
    
    // 테스트용 회사 그룹 생성 헬퍼 메소드
    private CompanyGroup createCompanyGroup(String name, IndustryType industryType, Address address) {
        CompanyGroup group = mock(CompanyGroup.class);
        when(group.getName()).thenReturn(name);
        when(group.getTypeName()).thenReturn(industryType.getDescription());
        when(group.getAddress()).thenReturn(address);
        return group;
    }
    
    // 테스트용 학교 그룹 생성 헬퍼 메소드
    private SchoolGroup createSchoolGroup(String name, SchoolType schoolType, Address address) {
        SchoolGroup group = mock(SchoolGroup.class);
        when(group.getName()).thenReturn(name);
        when(group.getTypeName()).thenReturn(schoolType.name());
        when(group.getAddress()).thenReturn(address);
        return group;
    }
} 