package com.stcom.smartmealtable.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.stcom.smartmealtable.domain.Budget.DailyBudget;
import com.stcom.smartmealtable.domain.Budget.MonthlyBudget;
import com.stcom.smartmealtable.service.BudgetService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig
class MemberBudgetControllerTest extends ControllerTestSupport {

    @Mock
    private BudgetService budgetService;

    @BeforeEach
    void setUpTest() {
        budgetService = Mockito.mock(BudgetService.class);
        MemberBudgetController controller = new MemberBudgetController(budgetService);
        super.setUp(controller);
    }

    @Test
    @DisplayName("GET /daily/{date} - 일별 예산 조회")
    void dailyBudget() throws Exception {
        DailyBudget budget = Mockito.mock(DailyBudget.class);
        when(budget.getSpendAmount()).thenReturn(BigDecimal.valueOf(1000));
        when(budget.getLimit()).thenReturn(BigDecimal.valueOf(10000));
        when(budget.getAvailableAmount()).thenReturn(BigDecimal.valueOf(9000));
        when(budgetService.getDailyBudgetBy(anyLong(), any(LocalDate.class))).thenReturn(budget);

        mockMvc.perform(get("/api/v1/members/me/budgets/daily/2025-06-12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("PUT /daily/{date}/default - 기본 일별 예산 등록")
    void registerDefaultDaily() throws Exception {
        mockMvc.perform(put("/api/v1/members/me/budgets/daily/2025-06-12/default")
                        .param("limit", "10000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("PATCH /daily/{date} - 일별 예산 수정")
    void editDaily() throws Exception {
        mockMvc.perform(patch("/api/v1/members/me/budgets/daily/2025-06-12")
                        .param("limit", "15000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("GET /daily/{date}/week - 주간 일별 예산 리스트")
    void dailyWeek() throws Exception {
        DailyBudget budget = Mockito.mock(DailyBudget.class);
        when(budget.getSpendAmount()).thenReturn(BigDecimal.valueOf(1000));
        when(budget.getLimit()).thenReturn(BigDecimal.valueOf(10000));
        when(budget.getAvailableAmount()).thenReturn(BigDecimal.valueOf(9000));
        when(budgetService.getDailyBudgetsByWeek(anyLong(), any(LocalDate.class))).thenReturn(List.of(budget));

        mockMvc.perform(get("/api/v1/members/me/budgets/daily/2025-06-12/week"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("GET /monthly/{yearMonth} - 월별 예산 조회")
    void monthly() throws Exception {
        MonthlyBudget mb = Mockito.mock(MonthlyBudget.class);
        when(mb.getSpendAmount()).thenReturn(BigDecimal.valueOf(1000));
        when(mb.getLimit()).thenReturn(BigDecimal.valueOf(10000));
        when(mb.getAvailableAmount()).thenReturn(BigDecimal.valueOf(9000));
        when(budgetService.getMonthlyBudgetBy(anyLong(), any(YearMonth.class))).thenReturn(mb);

        mockMvc.perform(get("/api/v1/members/me/budgets/monthly/2025-06"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("PUT /monthly/{yearMonth}/default - 기본 월별 예산 등록")
    void registerDefaultMonthly() throws Exception {
        mockMvc.perform(put("/api/v1/members/me/budgets/monthly/2025-06/default")
                        .param("limit", "300000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("PATCH /monthly/{yearMonth} - 월별 예산 수정")
    void editMonthly() throws Exception {
        mockMvc.perform(patch("/api/v1/members/me/budgets/monthly/2025-06")
                        .param("limit", "350000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("GET /montly - 이전 6개월 월별 예산 조회")
    void monthlyBudgetsPreviousMonths() throws Exception {
        MonthlyBudget mb1 = Mockito.mock(MonthlyBudget.class);
        when(mb1.getSpendAmount()).thenReturn(BigDecimal.valueOf(1000));
        when(mb1.getLimit()).thenReturn(BigDecimal.valueOf(10000));
        when(mb1.getAvailableAmount()).thenReturn(BigDecimal.valueOf(9000));
        
        MonthlyBudget mb2 = Mockito.mock(MonthlyBudget.class);
        when(mb2.getSpendAmount()).thenReturn(BigDecimal.valueOf(2000));
        when(mb2.getLimit()).thenReturn(BigDecimal.valueOf(15000));
        when(mb2.getAvailableAmount()).thenReturn(BigDecimal.valueOf(13000));
        
        when(budgetService.getMonthlyBudgetsBy(anyLong(), any(LocalDate.class), anyInt())).thenReturn(List.of(mb1, mb2));

        mockMvc.perform(get("/api/v1/members/me/budgets/montly/2025-06-12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }
} 