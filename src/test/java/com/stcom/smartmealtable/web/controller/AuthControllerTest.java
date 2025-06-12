package com.stcom.smartmealtable.web.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stcom.smartmealtable.domain.member.Member;
import com.stcom.smartmealtable.infrastructure.dto.JwtTokenResponseDto;
import com.stcom.smartmealtable.security.JwtTokenService;
import com.stcom.smartmealtable.service.MemberService;
import com.stcom.smartmealtable.service.TermService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

@SpringJUnitConfig
class AuthControllerTest extends ControllerTestSupport {

    private MemberService memberService;
    private JwtTokenService jwtTokenService;
    private TermService termService;
    private final ObjectMapper om = new ObjectMapper();

    @BeforeEach
    void init() {
        memberService = Mockito.mock(MemberService.class);
        jwtTokenService = Mockito.mock(JwtTokenService.class);
        termService = Mockito.mock(TermService.class);
        
        AuthController controller = new AuthController(memberService, jwtTokenService, termService);
        super.setUp(controller);
    }

    @Test
    @DisplayName("GET /api/v1/auth/email/check - 이메일 중복 확인 성공")
    void checkEmail_Available() throws Exception {
        // given
        doNothing().when(memberService).validateDuplicatedEmail(anyString());

        // when & then
        mockMvc.perform(get("/api/v1/auth/email/check")
                        .param("email", "available@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
        
        verify(memberService).validateDuplicatedEmail("available@example.com");
    }

    @Test
    @DisplayName("GET /api/v1/auth/email/check - 유효하지 않은 이메일 형식")
    void checkEmail_InvalidFormat() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/auth/email/check")
                        .param("email", "invalid-email"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/auth/signup - 회원가입 성공")
    void signUp_Success() throws Exception {
        // given
        JwtTokenResponseDto tokenDto = new JwtTokenResponseDto("accessToken", "refreshToken", 3600, "Bearer");
        
        doNothing().when(memberService).validateDuplicatedEmail(anyString());
        doNothing().when(memberService).checkPasswordDoubly(anyString(), anyString());
        
        // Member를 저장할 때 ID를 설정하는 동작을 모킹
        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        doAnswer(invocation -> {
            Member member = invocation.getArgument(0);
            // Reflection을 사용하여 ID 설정 (테스트에서만 사용)
            ReflectionTestUtils.setField(member, "id", 1L);
            return null;
        }).when(memberService).saveMember(memberCaptor.capture());
        
        when(jwtTokenService.createTokenDto(eq(1L), isNull())).thenReturn(tokenDto);

        String requestBody = om.writeValueAsString(
                new AuthController.SignUpRequest("test@example.com", "password123!", "password123!", "테스트 사용자"));

        // when & then
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.accessToken").value("accessToken"))
                .andExpect(jsonPath("$.data.newUser").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/auth/signup - 유효하지 않은 이메일 형식으로 회원가입 실패")
    void signUp_InvalidEmail() throws Exception {
        // given
        String requestBody = om.writeValueAsString(
                new AuthController.SignUpRequest("invalid-email", "password123!", "password123!", "테스트 사용자"));

        // when & then
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/auth/signup/terms - 약관 동의")
    void agreeTerms() throws Exception {
        // given
        List<AuthController.TermAgreementRequest> agreements = List.of(
                new AuthController.TermAgreementRequest(1L, true),
                new AuthController.TermAgreementRequest(2L, false)
        );
        
        String requestBody = om.writeValueAsString(agreements);

        // when & then
        mockMvc.perform(post("/api/v1/auth/signup/terms")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        verify(termService).agreeTerms(eq(1L), anyList());
    }

    @Test
    @DisplayName("DELETE /api/v1/auth/signup - 회원가입 취소")
    void cancelSignUp() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/v1/auth/signup"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        verify(memberService).deleteByMemberId(1L);
    }

    @Test
    @DisplayName("POST /api/v1/auth/signup - 빈 이름으로 회원가입 실패")
    void signUp_EmptyFullName() throws Exception {
        // given
        String requestBody = om.writeValueAsString(
                new AuthController.SignUpRequest("test@example.com", "password123!", "password123!", ""));

        // when & then
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/auth/signup/terms - 빈 약관 동의 목록")
    void agreeTerms_EmptyList() throws Exception {
        // given
        String requestBody = "[]";

        // when & then
        mockMvc.perform(post("/api/v1/auth/signup/terms")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        verify(termService).agreeTerms(eq(1L), anyList());
    }
} 