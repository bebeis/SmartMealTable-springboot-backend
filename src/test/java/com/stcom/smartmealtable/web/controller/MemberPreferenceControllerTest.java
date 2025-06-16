package com.stcom.smartmealtable.web.controller;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stcom.smartmealtable.domain.food.FoodCategory;
import com.stcom.smartmealtable.domain.food.MemberCategoryPreference;
import com.stcom.smartmealtable.domain.food.PreferenceType;
import com.stcom.smartmealtable.service.MemberCategoryPreferenceService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig
class MemberPreferenceControllerTest extends ControllerTestSupport {

    private MemberCategoryPreferenceService preferenceService;
    private final ObjectMapper om = new ObjectMapper();

    @BeforeEach
    void init() {
        preferenceService = Mockito.mock(MemberCategoryPreferenceService.class);
        MemberPreferenceController controller = new MemberPreferenceController(preferenceService);
        super.setUp(controller);
    }

    @Test
    @DisplayName("GET /api/v1/members/me/preferences - 선호 카테고리 조회")
    void getPreferences() throws Exception {
        MemberCategoryPreference pref = Mockito.mock(MemberCategoryPreference.class);
        FoodCategory cat = Mockito.mock(FoodCategory.class);
        Mockito.when(cat.getId()).thenReturn(1L);
        Mockito.when(cat.getName()).thenReturn("카테고리");
        when(pref.getType()).thenReturn(PreferenceType.LIKE);
        when(pref.getPriority()).thenReturn(1);
        when(pref.getCategory()).thenReturn(cat);
        when(preferenceService.getPreferences(anyLong())).thenReturn(List.of(pref));

        mockMvc.perform(get("/api/v1/members/me/preferences"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("POST /api/v1/members/me/preferences - 선호 카테고리 저장")
    void savePreferences() throws Exception {
        String body = "{\"liked\":[1,2], \"disliked\":[3]}";
        mockMvc.perform(post("/api/v1/members/me/preferences")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }
} 