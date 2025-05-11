package com.stcom.smartmealtable.domain.Budget;

import static org.assertj.core.api.Assertions.assertThat;

import com.stcom.smartmealtable.domain.member.MemberAuth;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import org.junit.jupiter.api.Test;

class BudgetTest {

    @Test
    void 예산_생성() throws Exception {

        // given
        MemberAuth memberAuth = getMemberAuth();
        // when
        DailyBudget budget1 = new DailyBudget(memberAuth, BigDecimal.valueOf(100000), LocalDate.now());
        MonthlyBudget budget2 = new MonthlyBudget(memberAuth, BigDecimal.valueOf(100000), YearMonth.now());
        // then
        assertThat(budget1.getLimit()).isEqualTo(BigDecimal.valueOf(100000));
        assertThat(budget1.getDate()).isNotNull();

        assertThat(budget2.getLimit()).isEqualTo(BigDecimal.valueOf(100000));
    }

    private MemberAuth getMemberAuth() {
        return new MemberAuth();
    }

    @Test
    void 예산_소비_정수() throws Exception {
        // given
        MemberAuth memberAuth = getMemberAuth();
        Budget budget = new DailyBudget(memberAuth, BigDecimal.valueOf(100000), LocalDate.now());
        // when
        budget.addSpent(1000);
        // then
        assertThat(budget.getSpendAmount()).isEqualTo(BigDecimal.valueOf(1000));
        assertThat(budget.getAvailableAmount()).isEqualTo(BigDecimal.valueOf(99000));
    }

    @Test
    void 예산_소비_소수() throws Exception {
        // given
        MemberAuth memberAuth = getMemberAuth();
        Budget budget = new DailyBudget(memberAuth, BigDecimal.valueOf(100000), LocalDate.now());
        // when
        budget.addSpent(9999.9);
        // then
        assertThat(budget.getSpendAmount()).isEqualTo(BigDecimal.valueOf(9999.9));
        assertThat(budget.getAvailableAmount()).isEqualTo(BigDecimal.valueOf(90000.1));
    }

    @Test
    void 예산_초과_유무() throws Exception {
        // given
        MemberAuth memberAuth1 = getMemberAuth();
        MemberAuth memberAuth2 = getMemberAuth();
        Budget budget1 = new DailyBudget(memberAuth1, BigDecimal.valueOf(100000), LocalDate.now());
        Budget budget2 = new DailyBudget(memberAuth2, BigDecimal.valueOf(100000), LocalDate.now());

        // when
        budget1.addSpent(99000);
        budget2.addSpent(110000);

        // then
        assertThat(budget1.isOverLimit()).isFalse();
        assertThat(budget2.isOverLimit()).isTrue();

    }
}