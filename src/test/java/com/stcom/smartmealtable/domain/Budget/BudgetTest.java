package com.stcom.smartmealtable.domain.Budget;

import static org.assertj.core.api.Assertions.assertThat;

import com.stcom.smartmealtable.domain.member.Member;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import org.junit.jupiter.api.Test;

class BudgetTest {

    @Test
    void 예산_생성() throws Exception {

        // given
        Member member = getMember();
        // when
        DailyBudget budget1 = new DailyBudget(member, BigDecimal.valueOf(100000), LocalDate.now());
        MonthlyBudget budget2 = new MonthlyBudget(member, BigDecimal.valueOf(100000), YearMonth.now());
        // then
        assertThat(budget1.getLimit()).isEqualTo(BigDecimal.valueOf(100000));
        assertThat(budget1.getDate()).isNotNull();

        assertThat(budget2.getLimit()).isEqualTo(BigDecimal.valueOf(100000));
    }

    private Member getMember() {
        return new Member();
    }

    @Test
    void 예산_소비_정수() throws Exception {
        // given
        Member member = getMember();
        Budget budget = new DailyBudget(member, BigDecimal.valueOf(100000), LocalDate.now());
        // when
        budget.addSpent(1000);
        // then
        assertThat(budget.getSpendAmount()).isEqualTo(BigDecimal.valueOf(1000));
        assertThat(budget.getAvailableAmount()).isEqualTo(BigDecimal.valueOf(99000));
    }

    @Test
    void 예산_소비_소수() throws Exception {
        // given
        Member member = getMember();
        Budget budget = new DailyBudget(member, BigDecimal.valueOf(100000), LocalDate.now());
        // when
        budget.addSpent(9999.9);
        // then
        assertThat(budget.getSpendAmount()).isEqualTo(BigDecimal.valueOf(9999.9));
        assertThat(budget.getAvailableAmount()).isEqualTo(BigDecimal.valueOf(90000.1));
    }

    @Test
    void 예산_초과_유무() throws Exception {
        // given
        Member member1 = getMember();
        Member member2 = getMember();
        Budget budget1 = new DailyBudget(member1, BigDecimal.valueOf(100000), LocalDate.now());
        Budget budget2 = new DailyBudget(member2, BigDecimal.valueOf(100000), LocalDate.now());

        // when
        budget1.addSpent(99000);
        budget2.addSpent(110000);

        // then
        assertThat(budget1.isOverLimit()).isFalse();
        assertThat(budget2.isOverLimit()).isTrue();

    }
}