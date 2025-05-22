package com.stcom.smartmealtable.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stcom.smartmealtable.domain.member.Member;
import com.stcom.smartmealtable.infrastructure.dto.JwtTokenResponseDto;
import com.stcom.smartmealtable.security.JwtTokenService;
import com.stcom.smartmealtable.service.MemberService;
import com.stcom.smartmealtable.service.TermService;
import com.stcom.smartmealtable.service.dto.MemberDto;
import com.stcom.smartmealtable.web.argumentresolver.UserContext;
import com.stcom.smartmealtable.web.argumentresolver.UserContextArgumentResolver;
import com.stcom.smartmealtable.web.controller.MemberController.CreateMemberRequest;
import com.stcom.smartmealtable.web.controller.MemberController.EditMemberRequest;
import com.stcom.smartmealtable.web.controller.MemberController.TermAgreementDto;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MemberService memberService;

    @MockBean
    private JwtTokenService jwtTokenService;

    @MockBean
    private TermService termService;
    
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
        when(jwtTokenService.extractClaims(anyString())).thenReturn(claims);
    }

    @Test
    @DisplayName("이메일 중복 확인 API가 정상적으로 동작해야 한다")
    void checkEmail() throws Exception {
        // given
        String email = "test@example.com";
        doNothing().when(memberService).validateDuplicatedEmail(email);

        // when & then
        mockMvc.perform(get("/api/v1/members/email/check")
                        .param("email", email))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }
    
    @Test
    @DisplayName("회원 생성 API가 정상적으로 동작해야 한다")
    void createMember() throws Exception {
        // given
        CreateMemberRequest request = new CreateMemberRequest(
                "test@example.com", "Password123!", "Password123!", "홍길동");
        
        // Member 모킹
        Member mockMember = mock(Member.class);
        when(mockMember.getId()).thenReturn(1L);
        
        // Member Builder 모킹
        Member.MemberBuilder mockBuilder = mock(Member.MemberBuilder.class);
        when(mockBuilder.fullName(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.email(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.rawPassword(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenReturn(mockMember);
        
        // Member 정적 메소드 모킹
        try (var mockStatic = Mockito.mockStatic(Member.class)) {
            mockStatic.when(Member::builder).thenReturn(mockBuilder);
            
            JwtTokenResponseDto tokenDto = new JwtTokenResponseDto(
                "test-access-token", 
                "test-refresh-token", 
                3600, 
                "Bearer"
            );
            tokenDto.setNewUser(true);
            
            // MemberService 모킹
            doNothing().when(memberService).validateDuplicatedEmail(anyString());
            doNothing().when(memberService).checkPasswordDoubly(anyString(), anyString());
            doNothing().when(memberService).saveMember(any(Member.class));
            
            // JwtTokenService 모킹
            when(jwtTokenService.createTokenDto(anyLong(), any())).thenReturn(tokenDto);
    
            // when & then
            mockMvc.perform(post("/api/v1/members")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.accessToken").value("test-access-token"))
                    .andExpect(jsonPath("$.data.refreshToken").value("test-refresh-token"))
                    .andExpect(jsonPath("$.data.newUser").value(true));
        }
    }

    @Test
    @DisplayName("회원 정보 수정 API가 정상적으로 동작해야 한다")
    void editMember() throws Exception {
        // given
        EditMemberRequest request = new EditMemberRequest(
                "OldPassword123!", "NewPassword123!", "NewPassword123!");
                
        doNothing().when(memberService).checkPasswordDoubly(anyString(), anyString());
        doNothing().when(memberService).changePassword(anyLong(), anyString(), anyString());

        // when & then
        mockMvc.perform(patch("/api/v1/members/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer test-token"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("회원 탈퇴 API가 정상적으로 동작해야 한다")
    void deleteMember() throws Exception {
        // given
        doNothing().when(memberService).deleteByMemberId(anyLong());

        // when & then
        mockMvc.perform(delete("/api/v1/members/me")
                        .header("Authorization", "Bearer test-token"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("약관 동의 API가 정상적으로 동작해야 한다")
    void signUpWithTermAgreement() throws Exception {
        // given
        List<TermAgreementDto> agreements = Arrays.asList(
                new TermAgreementDto(1L, true),
                new TermAgreementDto(2L, false)
        );
        
        doNothing().when(termService).agreeTerms(anyLong(), any());

        // when & then
        mockMvc.perform(post("/api/v1/members/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(agreements))
                        .header("Authorization", "Bearer test-token"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }
} 