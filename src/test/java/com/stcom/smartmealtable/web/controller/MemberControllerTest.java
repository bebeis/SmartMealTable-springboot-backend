package com.stcom.smartmealtable.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import com.stcom.smartmealtable.web.controller.MemberController.CreateMemberRequest;
import com.stcom.smartmealtable.web.controller.MemberController.EditMemberRequest;
import com.stcom.smartmealtable.web.controller.MemberController.TermAgreementDto;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(value = MemberController.class,
        includeFilters = {
                @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = {RestController.class})
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

    @Test
    @DisplayName("이메일 중복 확인 API가 정상적으로 동작해야 한다")
    void checkEmail() throws Exception {
        // given
        String email = "test@example.com";
        doNothing().when(memberService).validateDuplicatedEmail(email);

        // when & then
        mockMvc.perform(get("/api/v1/members/email/check")
                        .param("email", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("회원 생성 API가 정상적으로 동작해야 한다")
    void createMember() throws Exception {
        // given
        CreateMemberRequest request = new CreateMemberRequest(
                "test@example.com", "Password123!", "Password123!", "홍길동");
        
        JwtTokenResponseDto tokenDto = new JwtTokenResponseDto(
            "test-access-token", 
            "test-refresh-token", 
            3600, 
            "Bearer"
        );
        tokenDto.setNewUser(true);
        
        when(jwtTokenService.createTokenDto(anyLong(), any())).thenReturn(tokenDto);
        doNothing().when(memberService).validateDuplicatedEmail(anyString());
        doNothing().when(memberService).checkPasswordDoubly(anyString(), anyString());

        // when & then
        mockMvc.perform(post("/api/v1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("test-access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("test-refresh-token"))
                .andExpect(jsonPath("$.data.newUser").value(true));
    }

    @Test
    @DisplayName("회원 정보 수정 API가 정상적으로 동작해야 한다")
    void editMember() throws Exception {
        // given
        EditMemberRequest request = new EditMemberRequest(
                "OldPassword123!", "NewPassword123!", "NewPassword123!");
        
        MemberDto memberDto = MemberDto.builder()
                .memberId(1L)
                .build();
        
        // when & then
        mockMvc.perform(patch("/api/v1/members/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .requestAttr("memberDto", memberDto)) // UserContext를 모킹
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("회원 탈퇴 API가 정상적으로 동작해야 한다")
    void deleteMember() throws Exception {
        // given
        MemberDto memberDto = MemberDto.builder()
                .memberId(1L)
                .build();
        
        doNothing().when(memberService).deleteByMemberId(anyLong());

        // when & then
        mockMvc.perform(delete("/api/v1/members/me")
                        .requestAttr("memberDto", memberDto)) // UserContext를 모킹
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("약관 동의 API가 정상적으로 동작해야 한다")
    void signUpWithTermAgreement() throws Exception {
        // given
        List<TermAgreementDto> agreements = Arrays.asList(
                new TermAgreementDto(1L, true),
                new TermAgreementDto(2L, false)
        );
        
        MemberDto memberDto = MemberDto.builder()
                .memberId(1L)
                .build();
        
        doNothing().when(termService).agreeTerms(anyLong(), any());

        // when & then
        mockMvc.perform(post("/api/v1/members/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(agreements))
                        .requestAttr("memberDto", memberDto)) // UserContext를 모킹
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
} 