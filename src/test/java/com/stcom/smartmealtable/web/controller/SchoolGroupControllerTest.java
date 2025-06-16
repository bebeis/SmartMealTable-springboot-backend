package com.stcom.smartmealtable.web.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stcom.smartmealtable.domain.group.SchoolType;
import com.stcom.smartmealtable.service.GroupService;
import com.stcom.smartmealtable.web.dto.group.SchoolGroupCreateRequest;
import com.stcom.smartmealtable.web.dto.group.SchoolGroupUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig
class SchoolGroupControllerTest extends ControllerTestSupport {

    private GroupService groupService;
    private final ObjectMapper om = new ObjectMapper();

    @BeforeEach
    void init() {
        groupService = Mockito.mock(GroupService.class);
        SchoolGroupController controller = new SchoolGroupController(groupService);
        super.setUp(controller);
    }

    @Test
    @DisplayName("POST /api/v1/schools - 학교 그룹 등록")
    void registerSchool() throws Exception {
        SchoolGroupCreateRequest req = new SchoolGroupCreateRequest("road", "detail", "학교", SchoolType.UNIVERSITY_FOUR_YEAR);
        mockMvc.perform(post("/api/v1/schools")
                        .contentType("application/json")
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("PATCH /api/v1/schools/{id} - 학교 그룹 수정")
    void editSchool() throws Exception {
        SchoolGroupUpdateRequest req = new SchoolGroupUpdateRequest("road", "detail", "학교", SchoolType.UNIVERSITY_FOUR_YEAR);
        mockMvc.perform(patch("/api/v1/schools/1")
                        .contentType("application/json")
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("DELETE /api/v1/schools/{id} - 학교 그룹 삭제")
    void deleteSchool() throws Exception {
        mockMvc.perform(delete("/api/v1/schools/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }
} 