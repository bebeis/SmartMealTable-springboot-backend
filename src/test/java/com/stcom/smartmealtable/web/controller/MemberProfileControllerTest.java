package com.stcom.smartmealtable.web.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stcom.smartmealtable.domain.group.Group;
import com.stcom.smartmealtable.domain.Address.Address;
import com.stcom.smartmealtable.domain.Address.AddressEntity;
import com.stcom.smartmealtable.domain.Address.AddressType;
import com.stcom.smartmealtable.domain.member.MemberProfile;
import com.stcom.smartmealtable.domain.member.Member;
import com.stcom.smartmealtable.domain.member.MemberType;
import com.stcom.smartmealtable.service.MemberProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig
class MemberProfileControllerTest extends ControllerTestSupport {

    private MemberProfileService memberProfileService;
    private final ObjectMapper om = new ObjectMapper();

    @BeforeEach
    void init() {
        memberProfileService = Mockito.mock(MemberProfileService.class);
        MemberProfileController controller = new MemberProfileController(memberProfileService);
        super.setUp(controller);
    }

    @Test
    @DisplayName("GET /api/v1/members/profiles/me - 회원 프로필 페이지 정보 조회")
    void getMemberProfilePageInfo() throws Exception {
        // given
        Group group = Mockito.mock(Group.class);
        when(group.getName()).thenReturn("테스트 그룹");
        
        Address address = Address.builder()
                .roadAddress("도로명주소")
                .detailAddress("상세주소")
                .build();
        AddressEntity addressEntity = AddressEntity.builder()
                .address(address)
                .alias("집")
                .type(AddressType.HOME)
                .build();
        addressEntity.markPrimary();
        
        Member member = Mockito.mock(Member.class);
        MemberProfile profile = MemberProfile.builder()
                .member(member)
                .nickName("테스트닉네임")
                .type(MemberType.STUDENT)
                .group(group)
                .build();
        profile.addAddress(addressEntity);
        
        when(memberProfileService.getProfileFetch(anyLong())).thenReturn(profile);

        // when & then
        mockMvc.perform(get("/api/v1/members/profiles/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.nickName").value("테스트닉네임"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.memberType").value("STUDENT"))
                .andExpect(jsonPath("$.data.groupName").value("테스트 그룹"));
    }

    @Test
    @DisplayName("POST /api/v1/members/profiles - 회원 프로필 생성")
    void createMemberProfile() throws Exception {
        // given
        String requestBody = om.writeValueAsString(
                new MemberProfileController.MemberProfileRequest("새닉네임", 1L, MemberType.WORKER)
        );

        // when & then
        mockMvc.perform(post("/api/v1/members/profiles")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        verify(memberProfileService).createProfile("새닉네임", 1L, MemberType.WORKER, 1L);
    }

    @Test
    @DisplayName("PATCH /api/v1/members/profiles/me - 회원 프로필 수정")
    void changeMemberProfile() throws Exception {
        // given
        String requestBody = om.writeValueAsString(
                new MemberProfileController.MemberProfileRequest("수정된닉네임", 2L, MemberType.STUDENT)
        );

        // when & then
        mockMvc.perform(patch("/api/v1/members/profiles/me")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        verify(memberProfileService).changeProfile(1L, "수정된닉네임", MemberType.STUDENT, 2L);
    }

    @Test
    @DisplayName("POST /api/v1/members/profiles - 유효하지 않은 요청으로 프로필 생성 실패")
    void createMemberProfile_ValidationError() throws Exception {
        // given - 빈 닉네임
        String requestBody = "{\"nickName\":\"\", \"groupId\":1, \"memberType\":\"STUDENT\"}";

        // when & then
        mockMvc.perform(post("/api/v1/members/profiles")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/v1/members/profiles/me - 유효하지 않은 요청으로 프로필 수정 실패")
    void changeMemberProfile_ValidationError() throws Exception {
        // given - 빈 닉네임
        String requestBody = "{\"nickName\":\"\", \"groupId\":1, \"memberType\":\"STUDENT\"}";

        // when & then
        mockMvc.perform(patch("/api/v1/members/profiles/me")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }
} 