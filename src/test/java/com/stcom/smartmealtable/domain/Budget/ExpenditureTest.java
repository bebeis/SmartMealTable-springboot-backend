package com.stcom.smartmealtable.domain.Budget;

import static org.assertj.core.api.Assertions.assertThat;

import com.stcom.smartmealtable.domain.member.MemberProfile;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ExpenditureTest {

    private MemberProfile memberProfile;
    private DailyBudget dailyBudget;
    private MonthlyBudget monthlyBudget;

    @BeforeEach
    void setUp() {
        memberProfile = new MemberProfile();
        LocalDate today = LocalDate.now();
        YearMonth thisMonth = YearMonth.from(today);
        
        dailyBudget = new DailyBudget(memberProfile, BigDecimal.valueOf(50_000), today);
        monthlyBudget = new MonthlyBudget(memberProfile, BigDecimal.valueOf(1_000_000), thisMonth);
    }

    @Test
    @DisplayName("Expenditure 객체가 정상적으로 생성된다")
    void createExpenditure() {
        // given
        LocalDateTime spentDate = LocalDateTime.now();
        Long amount = 12000L;
        String tradeName = "Lunch";

        // when
        Expenditure expenditure = Expenditure.builder()
                .spentDate(spentDate)
                .amount(amount)
                .tradeName(tradeName)
                .dailyBudget(dailyBudget)
                .monthlyBudget(monthlyBudget)
                .build();

        // then
        assertThat(expenditure.getSpentDate()).isEqualTo(spentDate);
        assertThat(expenditure.getAmount()).isEqualTo(amount);
        assertThat(expenditure.getTradeName()).isEqualTo(tradeName);
        assertThat(expenditure.getDailyBudget()).isEqualTo(dailyBudget);
        assertThat(expenditure.getMonthlyBudget()).isEqualTo(monthlyBudget);
    }

    @Test
    @DisplayName("지출 내역 수정 시 예산에 차액이 반영된다")
    void editExpenditure_AmountChanged() {
        // given
        LocalDateTime originalSpentDate = LocalDateTime.now();
        Long originalAmount = 12000L;
        String originalTradeName = "Lunch";
        
        Expenditure expenditure = Expenditure.builder()
                .spentDate(originalSpentDate)
                .amount(originalAmount)
                .tradeName(originalTradeName)
                .dailyBudget(dailyBudget)
                .monthlyBudget(monthlyBudget)
                .build();
        
        // 초기 예산에 지출 추가
        dailyBudget.addSpent(BigDecimal.valueOf(originalAmount));
        monthlyBudget.addSpent(BigDecimal.valueOf(originalAmount));
        
        LocalDateTime newSpentDate = LocalDateTime.now().plusHours(1);
        Long newAmount = 15000L; // 3000원 증가
        String newTradeName = "Dinner";

        // when
        expenditure.edit(newSpentDate, newAmount, newTradeName);

        // then
        assertThat(expenditure.getSpentDate()).isEqualTo(newSpentDate);
        assertThat(expenditure.getAmount()).isEqualTo(newAmount);
        assertThat(expenditure.getTradeName()).isEqualTo(newTradeName);
        
        // 예산에 차액(3000원)이 추가로 반영되어야 함
        assertThat(dailyBudget.getSpendAmount()).isEqualTo(BigDecimal.valueOf(15000));
        assertThat(monthlyBudget.getSpendAmount()).isEqualTo(BigDecimal.valueOf(15000));
    }

    @Test
    @DisplayName("지출 금액을 줄여서 수정하면 예산에서 차액만큼 차감된다")
    void editExpenditure_AmountDecreased() {
        // given
        LocalDateTime originalSpentDate = LocalDateTime.now();
        Long originalAmount = 15000L;
        String originalTradeName = "Dinner";
        
        Expenditure expenditure = Expenditure.builder()
                .spentDate(originalSpentDate)
                .amount(originalAmount)
                .tradeName(originalTradeName)
                .dailyBudget(dailyBudget)
                .monthlyBudget(monthlyBudget)
                .build();
        
        // 초기 예산에 지출 추가
        dailyBudget.addSpent(BigDecimal.valueOf(originalAmount));
        monthlyBudget.addSpent(BigDecimal.valueOf(originalAmount));
        
        LocalDateTime newSpentDate = LocalDateTime.now().plusHours(1);
        Long newAmount = 10000L; // 5000원 감소
        String newTradeName = "Lunch";

        // when
        expenditure.edit(newSpentDate, newAmount, newTradeName);

        // then
        assertThat(expenditure.getAmount()).isEqualTo(newAmount);
        
        // 예산에서 차액(5000원)이 차감되어야 함
        assertThat(dailyBudget.getSpendAmount()).isEqualTo(BigDecimal.valueOf(10000));
        assertThat(monthlyBudget.getSpendAmount()).isEqualTo(BigDecimal.valueOf(10000));
    }

    @Test
    @DisplayName("지출 금액을 동일하게 수정해도 예산에 변화가 없다")
    void editExpenditure_SameAmount() {
        // given
        LocalDateTime originalSpentDate = LocalDateTime.now();
        Long originalAmount = 12000L;
        String originalTradeName = "Lunch";
        
        Expenditure expenditure = Expenditure.builder()
                .spentDate(originalSpentDate)
                .amount(originalAmount)
                .tradeName(originalTradeName)
                .dailyBudget(dailyBudget)
                .monthlyBudget(monthlyBudget)
                .build();
        
        // 초기 예산에 지출 추가
        dailyBudget.addSpent(BigDecimal.valueOf(originalAmount));
        monthlyBudget.addSpent(BigDecimal.valueOf(originalAmount));
        
        LocalDateTime newSpentDate = LocalDateTime.now().plusHours(1);
        Long newAmount = 12000L; // 동일한 금액
        String newTradeName = "Brunch";

        // when
        expenditure.edit(newSpentDate, newAmount, newTradeName);

        // then
        assertThat(expenditure.getSpentDate()).isEqualTo(newSpentDate);
        assertThat(expenditure.getAmount()).isEqualTo(newAmount);
        assertThat(expenditure.getTradeName()).isEqualTo(newTradeName);
        
        // 예산에 변화가 없어야 함
        assertThat(dailyBudget.getSpendAmount()).isEqualTo(BigDecimal.valueOf(originalAmount));
        assertThat(monthlyBudget.getSpendAmount()).isEqualTo(BigDecimal.valueOf(originalAmount));
    }

    @Test
    @DisplayName("지출 날짜와 상호명만 수정하고 금액은 그대로 두면 예산에 변화가 없다")
    void editExpenditure_OnlyDateAndTradeName() {
        // given
        LocalDateTime originalSpentDate = LocalDateTime.now();
        Long amount = 12000L;
        String originalTradeName = "Lunch";
        
        Expenditure expenditure = Expenditure.builder()
                .spentDate(originalSpentDate)
                .amount(amount)
                .tradeName(originalTradeName)
                .dailyBudget(dailyBudget)
                .monthlyBudget(monthlyBudget)
                .build();
        
        // 초기 예산에 지출 추가
        dailyBudget.addSpent(BigDecimal.valueOf(amount));
        monthlyBudget.addSpent(BigDecimal.valueOf(amount));
        
        BigDecimal originalDailySpent = dailyBudget.getSpendAmount();
        BigDecimal originalMonthlySpent = monthlyBudget.getSpendAmount();
        
        LocalDateTime newSpentDate = LocalDateTime.now().plusHours(2);
        String newTradeName = "Late Lunch";

        // when
        expenditure.edit(newSpentDate, amount, newTradeName);

        // then
        assertThat(expenditure.getSpentDate()).isEqualTo(newSpentDate);
        assertThat(expenditure.getAmount()).isEqualTo(amount);
        assertThat(expenditure.getTradeName()).isEqualTo(newTradeName);
        
        // 예산에 변화가 없어야 함
        assertThat(dailyBudget.getSpendAmount()).isEqualTo(originalDailySpent);
        assertThat(monthlyBudget.getSpendAmount()).isEqualTo(originalMonthlySpent);
    }
} 