package com.stcom.smartmealtable.domain.Budget;

import static org.assertj.core.api.Assertions.assertThat;

import com.stcom.smartmealtable.domain.member.MemberProfile;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DailyBudgetTest {

    @Test
    @DisplayName("DailyBudget 객체가 정상적으로 생성된다")
    void createDailyBudget() {
        // given
        MemberProfile memberProfile = new MemberProfile();
        BigDecimal limit = BigDecimal.valueOf(10000);
        LocalDate today = LocalDate.now();

        // when
        DailyBudget dailyBudget = new DailyBudget(memberProfile, limit, today);

        // then
        assertThat(dailyBudget.getMemberProfile()).isEqualTo(memberProfile);
        assertThat(dailyBudget.getLimit()).isEqualTo(limit);
        assertThat(dailyBudget.getDate()).isEqualTo(today);
        assertThat(dailyBudget.getSpendAmount()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("DailyBudget에 소비 금액을 추가할 수 있다")
    void addSpentAmount() {
        // given
        MemberProfile memberProfile = new MemberProfile();
        BigDecimal limit = BigDecimal.valueOf(10000);
        LocalDate today = LocalDate.now();
        DailyBudget dailyBudget = new DailyBudget(memberProfile, limit, today);

        // when
        dailyBudget.addSpent(BigDecimal.valueOf(3000));
        dailyBudget.addSpent(1000);
        dailyBudget.addSpent(1500.5);

        // then
        assertThat(dailyBudget.getSpendAmount()).isEqualTo(BigDecimal.valueOf(5500.5));
        assertThat(dailyBudget.getAvailableAmount()).isEqualTo(BigDecimal.valueOf(4499.5));
    }

    @Test
    @DisplayName("DailyBudget이 한도를 초과했는지 확인할 수 있다")
    void checkIfOverLimit() {
        // given
        MemberProfile memberProfile = new MemberProfile();
        BigDecimal limit = BigDecimal.valueOf(5000);
        LocalDate today = LocalDate.now();
        DailyBudget dailyBudget = new DailyBudget(memberProfile, limit, today);

        // when
        dailyBudget.addSpent(3000);
        boolean beforeOverLimit = dailyBudget.isOverLimit();
        
        dailyBudget.addSpent(2500);
        boolean afterOverLimit = dailyBudget.isOverLimit();

        // then
        assertThat(beforeOverLimit).isFalse();
        assertThat(afterOverLimit).isTrue();
        assertThat(dailyBudget.getAvailableAmount()).isEqualTo(BigDecimal.valueOf(-500));
    }

    @Test
    @DisplayName("DailyBudget의 소비 금액을 초기화할 수 있다")
    void resetSpentAmount() {
        // given
        MemberProfile memberProfile = new MemberProfile();
        BigDecimal limit = BigDecimal.valueOf(10000);
        LocalDate today = LocalDate.now();
        DailyBudget dailyBudget = new DailyBudget(memberProfile, limit, today);
        dailyBudget.addSpent(5000);

        // when
        dailyBudget.resetSpent();

        // then
        assertThat(dailyBudget.getSpendAmount()).isEqualTo(BigDecimal.ZERO);
        assertThat(dailyBudget.getAvailableAmount()).isEqualTo(limit);
    }
} 