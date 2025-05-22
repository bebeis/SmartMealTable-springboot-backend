package com.stcom.smartmealtable.web.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stcom.smartmealtable.domain.term.Term;
import com.stcom.smartmealtable.security.JwtTokenService;
import com.stcom.smartmealtable.service.TermService;
import java.util.Arrays;
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
class TermControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TermService termService;

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
    @DisplayName("모든 약관을 조회할 수 있다")
    void getTerms() throws Exception {
        // given
        Term term1 = createTerm(1L, "이용약관", "이용약관 내용입니다.", true);
        Term term2 = createTerm(2L, "개인정보 처리방침", "개인정보 처리방침 내용입니다.", true);
        Term term3 = createTerm(3L, "마케팅 정보 수신 동의", "마케팅 정보 수신 동의 내용입니다.", false);
        
        List<Term> terms = Arrays.asList(term1, term2, term3);
        
        when(termService.findAll()).thenReturn(terms);

        // when & then
        mockMvc.perform(get("/api/v1/terms")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].termId").value(1))
                .andExpect(jsonPath("$.data[0].title").value("이용약관"))
                .andExpect(jsonPath("$.data[0].content").value("이용약관 내용입니다."))
                .andExpect(jsonPath("$.data[0].required").value(true))
                .andExpect(jsonPath("$.data[1].termId").value(2))
                .andExpect(jsonPath("$.data[1].title").value("개인정보 처리방침"))
                .andExpect(jsonPath("$.data[1].content").value("개인정보 처리방침 내용입니다."))
                .andExpect(jsonPath("$.data[1].required").value(true))
                .andExpect(jsonPath("$.data[2].termId").value(3))
                .andExpect(jsonPath("$.data[2].title").value("마케팅 정보 수신 동의"))
                .andExpect(jsonPath("$.data[2].content").value("마케팅 정보 수신 동의 내용입니다."))
                .andExpect(jsonPath("$.data[2].required").value(false));
    }

    @Test
    @DisplayName("약관이 없는 경우 빈 배열을 반환한다")
    void getTerms_Empty() throws Exception {
        // given
        when(termService.findAll()).thenReturn(List.of());

        // when & then
        mockMvc.perform(get("/api/v1/terms")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }
    
    // 테스트용 약관 생성 헬퍼 메소드
    private Term createTerm(Long id, String title, String content, Boolean isRequired) {
        Term term = mock(Term.class);
        when(term.getId()).thenReturn(id);
        when(term.getTitle()).thenReturn(title);
        when(term.getContent()).thenReturn(content);
        when(term.getIsRequired()).thenReturn(isRequired);
        return term;
    }
} 