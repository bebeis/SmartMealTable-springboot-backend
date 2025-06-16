package com.stcom.smartmealtable.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stcom.smartmealtable.component.creditmessage.CreditMessageManager;
import com.stcom.smartmealtable.component.creditmessage.ExpenditureDto;
import com.stcom.smartmealtable.domain.Budget.Expenditure;
import com.stcom.smartmealtable.service.ExpenditureService;
import java.time.LocalDateTime;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig
class MemberExpenditureControllerTest extends ControllerTestSupport {

    private ExpenditureService expenditureService;
    private CreditMessageManager creditMessageManager;

    private final ObjectMapper om = new ObjectMapper();

    @BeforeEach
    void init() {
        expenditureService = Mockito.mock(ExpenditureService.class);
        creditMessageManager = Mockito.mock(CreditMessageManager.class);
        MemberExpenditureController controller = new MemberExpenditureController(expenditureService, creditMessageManager);
        super.setUp(controller);
    }

    @Test
    @DisplayName("POST /messages/parse - 카드 메시지 파싱")
    void parseMessage() throws Exception {
        when(creditMessageManager.parseMessage(any())).thenReturn(new ExpenditureDto("vendor", LocalDateTime.now(), 1000L, "trade"));
        mockMvc.perform(post("/api/v1/members/me/expenditures/messages/parse")
                        .contentType("application/json")
                        .content("{\"message\":\"some msg\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("GET /api/v1/members/me/expenditures - 목록 조회")
    void listExpenditures() throws Exception {
        Expenditure ex = Mockito.mock(Expenditure.class);
        Mockito.when(ex.getId()).thenReturn(1L);
        Mockito.when(ex.getSpentDate()).thenReturn(LocalDateTime.now());
        Mockito.when(ex.getAmount()).thenReturn(1000L);
        Mockito.when(ex.getTradeName()).thenReturn("점심");
        
        // Pageable.unpaged() 대신 PageRequest.of() 사용 (JSON 직렬화 문제 해결)
        Pageable pageable = PageRequest.of(0, 10);
        Slice<Expenditure> slice = new SliceImpl<>(Collections.singletonList(ex), pageable, false);
        when(expenditureService.getExpenditures(anyLong(), anyInt(), anyInt())).thenReturn(slice);

        mockMvc.perform(get("/api/v1/members/me/expenditures"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("POST /api/v1/members/me/expenditures - 지출 등록")
    void registerExpenditure() throws Exception {
        String body = "{\"spentDate\":\"2025-06-12T12:00:00\", \"amount\":1000, \"tradeName\":\"점심\"}";
        mockMvc.perform(post("/api/v1/members/me/expenditures")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("PATCH /{id} - 지출 수정")
    void editExpenditure() throws Exception {
        String body = "{\"spentDate\":\"2025-06-12T18:00:00\", \"amount\":1500, \"tradeName\":\"저녁\"}";
        mockMvc.perform(patch("/api/v1/members/me/expenditures/1")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("DELETE /{id} - 지출 삭제")
    void deleteExpenditure() throws Exception {
        mockMvc.perform(delete("/api/v1/members/me/expenditures/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }
} 