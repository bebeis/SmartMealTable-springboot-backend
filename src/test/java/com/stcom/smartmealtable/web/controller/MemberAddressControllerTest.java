package com.stcom.smartmealtable.web.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stcom.smartmealtable.infrastructure.AddressApiService;
import com.stcom.smartmealtable.domain.Address.Address;
import com.stcom.smartmealtable.domain.Address.AddressType;
import com.stcom.smartmealtable.service.MemberProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig
class MemberAddressControllerTest extends ControllerTestSupport {

    private MemberProfileService profileService;
    private AddressApiService addressApiService;
    private final ObjectMapper om = new ObjectMapper();

    @BeforeEach
    void init() {
        profileService = Mockito.mock(MemberProfileService.class);
        addressApiService = Mockito.mock(AddressApiService.class);
        MemberAddressController controller = new MemberAddressController(profileService, addressApiService);
        super.setUp(controller);
    }

    @Test
    @DisplayName("POST /{id}/primary - 기본 주소 변경")
    void changePrimary() throws Exception {
        mockMvc.perform(post("/api/v1/members/me/addresses/1/primary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("POST /api/v1/members/me/addresses - 주소 등록")
    void registerAddress() throws Exception {
        Address addr = Mockito.mock(Address.class);
        Mockito.when(addressApiService.createAddressFromRequest(Mockito.any())).thenReturn(addr);
        String body = "{\"roadAddress\":\"도로명\", \"addressType\":\"HOME\", \"alias\":\"집\", \"detailAddress\":\"101호\"}";
        mockMvc.perform(post("/api/v1/members/me/addresses")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("PATCH /{id} - 주소 수정")
    void editAddress() throws Exception {
        Address addr = Mockito.mock(Address.class);
        Mockito.when(addressApiService.createAddressFromRequest(Mockito.any())).thenReturn(addr);
        String body = "{\"roadAddress\":\"도로명\", \"addressType\":\"HOME\", \"alias\":\"학교\", \"detailAddress\":\"102호\"}";
        mockMvc.perform(patch("/api/v1/members/me/addresses/1")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("DELETE /{id} - 주소 삭제")
    void deleteAddress() throws Exception {
        mockMvc.perform(delete("/api/v1/members/me/addresses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }
} 