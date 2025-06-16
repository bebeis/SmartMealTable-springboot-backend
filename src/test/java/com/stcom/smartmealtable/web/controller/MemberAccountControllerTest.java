package com.stcom.smartmealtable.web.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stcom.smartmealtable.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig
class MemberAccountControllerTest extends ControllerTestSupport {

    private MemberService memberService;
    private final ObjectMapper om = new ObjectMapper();

    @BeforeEach
    void init() {
        memberService = Mockito.mock(MemberService.class);
        MemberAccountController controller = new MemberAccountController(memberService);
        super.setUp(controller);
    }

    @Test
    @DisplayName("PATCH /api/v1/members/me/password - 비밀번호 변경")
    void changePassword() throws Exception {
        String body = "{\"originPassword\":\"old\", \"newPassword\":\"newPass1!\", \"confirmPassword\":\"newPass1!\"}";
        mockMvc.perform(patch("/api/v1/members/me/password")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("DELETE /api/v1/members/me - 회원 탈퇴")
    void deleteMember() throws Exception {
        mockMvc.perform(delete("/api/v1/members/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }
} 