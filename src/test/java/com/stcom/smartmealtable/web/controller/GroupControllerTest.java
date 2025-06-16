package com.stcom.smartmealtable.web.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.stcom.smartmealtable.domain.group.Group;
import com.stcom.smartmealtable.domain.Address.Address;
import com.stcom.smartmealtable.service.GroupService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig
class GroupControllerTest extends ControllerTestSupport {

    private GroupService groupService;

    @BeforeEach
    void init() {
        groupService = Mockito.mock(GroupService.class);
        GroupController controller = new GroupController(groupService);
        super.setUp(controller);
    }

    @Test
    @DisplayName("GET /api/v1/groups?keyword= - 그룹 검색")
    void searchGroup() throws Exception {
        Group g = Mockito.mock(Group.class);
        Address addr = Mockito.mock(Address.class);
        Mockito.when(addr.getRoadAddress()).thenReturn("도로명");
        Mockito.when(g.getAddress()).thenReturn(addr);
        Mockito.when(g.getName()).thenReturn("그룹");
        Mockito.when(g.getTypeName()).thenReturn("TYPE");
        Mockito.when(g.getId()).thenReturn(1L);
        when(groupService.findGroupsByKeyword(anyString())).thenReturn(List.of(g));

        mockMvc.perform(get("/api/v1/groups").param("keyword", "school"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("DELETE /api/v1/groups/{id} - 그룹 삭제")
    void deleteGroup() throws Exception {
        mockMvc.perform(delete("/api/v1/groups/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }
} 