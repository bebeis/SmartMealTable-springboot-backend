package com.stcom.smartmealtable.domain.Budget;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.stcom.smartmealtable.domain.member.MemberProfile;
import java.math.BigDecimal;
import java.time.YearMonth;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MonthlyBudgetTest {

    @Test
    @DisplayName("월별 예산을 생성할 수 있다")
    void createMonthlyBudget() {
        // given
        MemberProfile memberProfile = mock(MemberProfile.class);
        BigDecimal limit = BigDecimal.valueOf(300000);
        YearMonth yearMonth = YearMonth.of(2025, 5);

        // when
        MonthlyBudget budget = new MonthlyBudget(memberProfile, limit, yearMonth);

        // then
        assertThat(budget.getMemberProfile()).isEqualTo(memberProfile);
        assertThat(budget.getLimit()).isEqualTo(limit);
        assertThat(budget.getYearMonth()).isEqualTo(yearMonth);
        assertThat(budget.getSpendAmount()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("지출 금액을 추가할 수 있다")
    void addSpent() {
        // given
        MemberProfile memberProfile = mock(MemberProfile.class);
        BigDecimal limit = BigDecimal.valueOf(300000);
        YearMonth yearMonth = YearMonth.of(2025, 5);
        MonthlyBudget budget = new MonthlyBudget(memberProfile, limit, yearMonth);

        // when
        budget.addSpent(BigDecimal.valueOf(50000));
        budget.addSpent(10000); // int 값 추가
        budget.addSpent(5000.5); // double 값 추가

        // then
        assertThat(budget.getSpendAmount()).isEqualTo(BigDecimal.valueOf(65000.5));
    }

    @Test
    @DisplayName("사용 가능한 예산 금액을 계산할 수 있다")
    void getAvailableAmount() {
        // given
        MemberProfile memberProfile = mock(MemberProfile.class);
        BigDecimal limit = BigDecimal.valueOf(300000);
        YearMonth yearMonth = YearMonth.of(2025, 5);
        MonthlyBudget budget = new MonthlyBudget(memberProfile, limit, yearMonth);

        // when
        budget.addSpent(BigDecimal.valueOf(100000));

        // then
        assertThat(budget.getAvailableAmount()).isEqualTo(BigDecimal.valueOf(200000));
    }

    @Test
    @DisplayName("예산 초과 여부를 확인할 수 있다")
    void isOverLimit() {
        // given
        MemberProfile memberProfile = mock(MemberProfile.class);
        BigDecimal limit = BigDecimal.valueOf(300000);
        YearMonth yearMonth = YearMonth.of(2025, 5);
        MonthlyBudget budget = new MonthlyBudget(memberProfile, limit, yearMonth);

        // when - 예산 이내 지출
        budget.addSpent(BigDecimal.valueOf(200000));
        
        // then
        assertThat(budget.isOverLimit()).isFalse();
        
        // when - 예산 초과 지출
        budget.addSpent(BigDecimal.valueOf(150000));
        
        // then
        assertThat(budget.isOverLimit()).isTrue();
    }
    
    @Test
    @DisplayName("지출 금액을 초기화할 수 있다")
    void resetSpent() {
        // given
        MemberProfile memberProfile = mock(MemberProfile.class);
        BigDecimal limit = BigDecimal.valueOf(300000);
        YearMonth yearMonth = YearMonth.of(2025, 5);
        MonthlyBudget budget = new MonthlyBudget(memberProfile, limit, yearMonth);
        budget.addSpent(BigDecimal.valueOf(100000));
        
        // when
        budget.resetSpent();
        
        // then
        assertThat(budget.getSpendAmount()).isEqualTo(BigDecimal.ZERO);
    }
} 