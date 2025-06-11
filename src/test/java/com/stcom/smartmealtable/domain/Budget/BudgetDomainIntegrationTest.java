package com.stcom.smartmealtable.domain.Budget;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import com.stcom.smartmealtable.domain.member.MemberProfile;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BudgetDomainIntegrationTest {

    private MemberProfile memberProfile;

    @BeforeEach
    void setUp() {
        memberProfile = new MemberProfile();
    }

    @DisplayName("예산 상속 구조와 다형성이 올바르게 작동한다")
    @Test
    void budgetPolymorphismTest() {
        // given
        List<Budget> budgets = new ArrayList<>();
        
        DailyBudget dailyBudget = new DailyBudget(memberProfile, BigDecimal.valueOf(20000), LocalDate.now());
        MonthlyBudget monthlyBudget = new MonthlyBudget(memberProfile, BigDecimal.valueOf(500000), YearMonth.now());
        
        budgets.add(dailyBudget);
        budgets.add(monthlyBudget);

        // when & then - 다형성을 통한 공통 동작 확인
        for (Budget budget : budgets) {
            // 초기 상태 검증
            assertThat(budget.getSpendAmount()).isEqualTo(BigDecimal.ZERO);
            assertThat(budget.isOverLimit()).isFalse();
            
            // 지출 추가
            budget.addSpent(BigDecimal.valueOf(10000));
            assertThat(budget.getSpendAmount()).isEqualTo(BigDecimal.valueOf(10000));
            
            // 사용 가능 금액 계산
            BigDecimal expectedAvailable = budget.getLimit().subtract(BigDecimal.valueOf(10000));
            assertThat(budget.getAvailableAmount()).isEqualTo(expectedAvailable);
        }

        // 각 타입별 고유 속성 확인
        assertThat(dailyBudget).isInstanceOf(DailyBudget.class);
        assertThat(monthlyBudget).isInstanceOf(MonthlyBudget.class);
        
        assertThat(dailyBudget.getDate()).isNotNull();
        assertThat(monthlyBudget.getYearMonth()).isNotNull();
    }

    @DisplayName("예산 한도 변경 시 비즈니스 로직이 올바르게 동작한다")
    @Test
    void budgetLimitChangeBusinessLogic() {
        // given
        DailyBudget budget = new DailyBudget(memberProfile, BigDecimal.valueOf(30000), LocalDate.now());
        budget.addSpent(20000); // 20,000원 지출

        // when & then - 한도 증가
        budget.changeLimit(BigDecimal.valueOf(50000));
        assertThat(budget.getLimit()).isEqualTo(BigDecimal.valueOf(50000));
        assertThat(budget.getAvailableAmount()).isEqualTo(BigDecimal.valueOf(30000));
        assertThat(budget.isOverLimit()).isFalse();

        // when & then - 한도 감소 (한도 초과 상황)
        budget.changeLimit(BigDecimal.valueOf(15000));
        assertThat(budget.getLimit()).isEqualTo(BigDecimal.valueOf(15000));
        assertThat(budget.getAvailableAmount()).isEqualTo(BigDecimal.valueOf(-5000));
        assertThat(budget.isOverLimit()).isTrue();
    }

    @DisplayName("복잡한 지출 패턴 계산")
    @Test
    void complexSpendingPatternCalculation() {
        // given
        DailyBudget weeklyBudget = new DailyBudget(
                memberProfile, 
                BigDecimal.valueOf(150000), 
                LocalDate.now()
        );

        // 다양한 금액으로 여러 번 지출
        weeklyBudget.addSpent(25000);
        weeklyBudget.addSpent(15000); 
        weeklyBudget.addSpent(7500);
        weeklyBudget.addSpent(12500); // 총 60,000원 지출

        // when
        BigDecimal totalSpent = weeklyBudget.getSpendAmount();
        BigDecimal remaining = weeklyBudget.getAvailableAmount();
        
        // 사용률 계산: 60,000 / 150,000 = 0.4 (40%)
        BigDecimal usageRate = totalSpent
                .divide(weeklyBudget.getLimit(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        // then - 정확한 계산 검증
        assertThat(totalSpent).isEqualTo(BigDecimal.valueOf(60000));
        assertThat(remaining).isEqualTo(BigDecimal.valueOf(90000)); // 150,000 - 60,000
        
        BigDecimal expectedUsageRate = BigDecimal.valueOf(40.00);
        assertThat(usageRate).isCloseTo(expectedUsageRate, within(BigDecimal.valueOf(0.01)));
    }

    @DisplayName("예산 리셋 기능")
    @Test
    void budgetResetFunctionality() {
        // given
        DailyBudget budget = new DailyBudget(
                memberProfile, 
                BigDecimal.valueOf(50000), 
                LocalDate.now()
        );
        
        budget.addSpent(30000);
        assertThat(budget.getSpendAmount()).isEqualTo(BigDecimal.valueOf(30000));

        // when - 예산 지출 리셋
        budget.resetSpent();

        // then
        assertThat(budget.getSpendAmount()).isEqualTo(BigDecimal.ZERO);
        assertThat(budget.getAvailableAmount()).isEqualTo(BigDecimal.valueOf(50000));
        
        // 사용률 = 0 / 50000 * 100 = 0% (0 나누기 문제 없음)
        BigDecimal usageRate = budget.getSpendAmount()
                .multiply(BigDecimal.valueOf(100))
                .divide(budget.getLimit(), 2, RoundingMode.HALF_UP);
        assertThat(usageRate).isEqualTo(BigDecimal.ZERO);
    }

    @DisplayName("예산의 부동소수점 정밀도 테스트")
    @Test
    void budgetFloatingPointPrecisionTest() {
        // given
        MonthlyBudget budget = new MonthlyBudget(
                memberProfile, 
                BigDecimal.valueOf(1000000), 
                YearMonth.of(2025, 6)
        );

        // when - 소수점을 포함한 복잡한 계산
        budget.addSpent(333333); // 33.3333%
        budget.addSpent(166667); // 추가로 16.6667%

        // then
        BigDecimal totalSpent = budget.getSpendAmount();
        
        // 사용률 계산: 500,000 / 1,000,000 = 0.5 (50%)
        BigDecimal usageRate = totalSpent
                .divide(budget.getLimit(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
        
        assertThat(totalSpent).isEqualTo(BigDecimal.valueOf(500000));
        
        BigDecimal expectedUsageRate = BigDecimal.valueOf(50.00);
        assertThat(usageRate).isCloseTo(expectedUsageRate, within(BigDecimal.valueOf(0.01)));
        
        // 남은 금액 검증
        assertThat(budget.getAvailableAmount()).isEqualTo(BigDecimal.valueOf(500000));
        assertThat(budget.isOverLimit()).isFalse();
    }

    @DisplayName("예산 한도 초과 경계값 테스트")
    @Test
    void budgetOverLimitBoundaryTest() {
        // given
        DailyBudget budget = new DailyBudget(memberProfile, BigDecimal.valueOf(100000), LocalDate.now());

        // when & then - 한도와 정확히 같은 금액 지출
        budget.addSpent(100000);
        assertThat(budget.isOverLimit()).isFalse();
        assertThat(budget.getAvailableAmount()).isEqualTo(BigDecimal.ZERO);

        // when & then - 1원 초과
        budget.addSpent(1);
        assertThat(budget.isOverLimit()).isTrue();
        assertThat(budget.getAvailableAmount()).isEqualTo(BigDecimal.valueOf(-1));

        // when & then - 리셋 후 1원 미만 지출
        budget.resetSpent();
        budget.addSpent(99999);
        assertThat(budget.isOverLimit()).isFalse();
        assertThat(budget.getAvailableAmount()).isEqualTo(BigDecimal.valueOf(1));
    }

    @DisplayName("월별 예산과 일일 예산의 독립성 확인")
    @Test
    void monthlyAndDailyBudgetIndependence() {
        // given
        YearMonth currentMonth = YearMonth.now();
        LocalDate today = LocalDate.now();
        
        MonthlyBudget monthlyBudget = new MonthlyBudget(memberProfile, BigDecimal.valueOf(500000), currentMonth);
        DailyBudget dailyBudget = new DailyBudget(memberProfile, BigDecimal.valueOf(20000), today);

        // when - 각각 다른 지출 추가
        monthlyBudget.addSpent(300000);
        dailyBudget.addSpent(15000);

        // then - 독립적인 계산 확인
        assertThat(monthlyBudget.getSpendAmount()).isEqualTo(BigDecimal.valueOf(300000));
        assertThat(monthlyBudget.getAvailableAmount()).isEqualTo(BigDecimal.valueOf(200000));
        assertThat(monthlyBudget.isOverLimit()).isFalse();

        assertThat(dailyBudget.getSpendAmount()).isEqualTo(BigDecimal.valueOf(15000));
        assertThat(dailyBudget.getAvailableAmount()).isEqualTo(BigDecimal.valueOf(5000));
        assertThat(dailyBudget.isOverLimit()).isFalse();

        // 서로의 상태에 영향을 주지 않음
        monthlyBudget.addSpent(250000); // 월별 예산 초과
        assertThat(monthlyBudget.isOverLimit()).isTrue();
        assertThat(dailyBudget.isOverLimit()).isFalse(); // 일일 예산은 영향 없음
    }

    @DisplayName("예산 객체의 불변 속성 확인")
    @Test
    void budgetImmutablePropertiesTest() {
        // given
        LocalDate testDate = LocalDate.of(2025, 6, 15);
        YearMonth testYearMonth = YearMonth.of(2025, 6);
        
        DailyBudget dailyBudget = new DailyBudget(memberProfile, BigDecimal.valueOf(30000), testDate);
        MonthlyBudget monthlyBudget = new MonthlyBudget(memberProfile, BigDecimal.valueOf(800000), testYearMonth);

        // when - 예산에 지출 추가 및 리셋
        dailyBudget.addSpent(20000);
        dailyBudget.resetSpent();
        dailyBudget.changeLimit(BigDecimal.valueOf(50000));

        monthlyBudget.addSpent(400000);
        monthlyBudget.resetSpent();
        monthlyBudget.changeLimit(BigDecimal.valueOf(1000000));

        // then - 기본 속성들은 변경되지 않음
        assertThat(dailyBudget.getDate()).isEqualTo(testDate);
        assertThat(dailyBudget.getMemberProfile()).isEqualTo(memberProfile);
        
        assertThat(monthlyBudget.getYearMonth()).isEqualTo(testYearMonth);
        assertThat(monthlyBudget.getMemberProfile()).isEqualTo(memberProfile);
    }

    @DisplayName("예산 생성 시 초기값 검증")
    @Test
    void budgetInitialValueValidation() {
        // given & when
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.now();
        BigDecimal limit = BigDecimal.valueOf(100000);
        
        DailyBudget dailyBudget = new DailyBudget(memberProfile, limit, today);
        MonthlyBudget monthlyBudget = new MonthlyBudget(memberProfile, limit, currentMonth);

        // then - 초기값 검증
        assertThat(dailyBudget.getSpendAmount()).isEqualTo(BigDecimal.ZERO);
        assertThat(dailyBudget.getLimit()).isEqualTo(limit);
        assertThat(dailyBudget.getAvailableAmount()).isEqualTo(limit);
        assertThat(dailyBudget.isOverLimit()).isFalse();
        assertThat(dailyBudget.getDate()).isEqualTo(today);
        assertThat(dailyBudget.getMemberProfile()).isEqualTo(memberProfile);

        assertThat(monthlyBudget.getSpendAmount()).isEqualTo(BigDecimal.ZERO);
        assertThat(monthlyBudget.getLimit()).isEqualTo(limit);
        assertThat(monthlyBudget.getAvailableAmount()).isEqualTo(limit);
        assertThat(monthlyBudget.isOverLimit()).isFalse();
        assertThat(monthlyBudget.getYearMonth()).isEqualTo(currentMonth);
        assertThat(monthlyBudget.getMemberProfile()).isEqualTo(memberProfile);
    }
} 