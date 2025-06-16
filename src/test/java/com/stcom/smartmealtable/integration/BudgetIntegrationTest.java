package com.stcom.smartmealtable.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.stcom.smartmealtable.domain.Budget.DailyBudget;
import com.stcom.smartmealtable.domain.Budget.MonthlyBudget;
import com.stcom.smartmealtable.domain.member.Member;
import com.stcom.smartmealtable.domain.member.MemberProfile;
import com.stcom.smartmealtable.repository.BudgetRepository;
import com.stcom.smartmealtable.repository.MemberProfileRepository;
import com.stcom.smartmealtable.repository.MemberRepository;
import com.stcom.smartmealtable.service.BudgetService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BudgetIntegrationTest {

    @Autowired
    private BudgetService budgetService;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberProfileRepository memberProfileRepository;

    private Member member;
    private MemberProfile memberProfile;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .email("integration@test.com")
                .rawPassword("testPassword!")
                .build();
        memberRepository.save(member);

        memberProfile = MemberProfile.builder()
                .nickName("integrationTestUser")
                .member(member)
                .build();
        memberProfileRepository.save(memberProfile);
    }

    @DisplayName("일일 예산 생성부터 지출 추가, 한도 초과 확인까지의 전체 시나리오 테스트")
    @Test
    void dailyBudgetCompleteScenario() {
        // given - 일일 예산 생성
        LocalDate today = LocalDate.now();
        Long dailyLimit = 30000L;
        DailyBudget dailyBudget = new DailyBudget(memberProfile, BigDecimal.valueOf(dailyLimit), today);
        budgetRepository.save(dailyBudget);

        // when & then - 1. 초기 상태 확인
        DailyBudget savedBudget = budgetService.getDailyBudgetBy(memberProfile.getId(), today);
        assertThat(savedBudget.getSpendAmount()).isEqualTo(BigDecimal.ZERO);
        assertThat(savedBudget.getAvailableAmount()).isEqualTo(BigDecimal.valueOf(dailyLimit));
        assertThat(savedBudget.isOverLimit()).isFalse();

        // when & then - 2. 첫 번째 지출 추가 (아직 한도 내)
        savedBudget.addSpent(15000);
        budgetRepository.save(savedBudget);
        
        DailyBudget afterFirstSpent = budgetService.getDailyBudgetBy(memberProfile.getId(), today);
        assertThat(afterFirstSpent.getSpendAmount()).isEqualTo(BigDecimal.valueOf(15000));
        assertThat(afterFirstSpent.getAvailableAmount()).isEqualTo(BigDecimal.valueOf(15000));
        assertThat(afterFirstSpent.isOverLimit()).isFalse();

        // when & then - 3. 두 번째 지출 추가 (한도 초과)
        afterFirstSpent.addSpent(20000);
        budgetRepository.save(afterFirstSpent);
        
        DailyBudget afterSecondSpent = budgetService.getDailyBudgetBy(memberProfile.getId(), today);
        assertThat(afterSecondSpent.getSpendAmount()).isEqualTo(BigDecimal.valueOf(35000));
        assertThat(afterSecondSpent.getAvailableAmount()).isEqualTo(BigDecimal.valueOf(-5000));
        assertThat(afterSecondSpent.isOverLimit()).isTrue();

        // when & then - 4. 지출 리셋
        afterSecondSpent.resetSpent();
        budgetRepository.save(afterSecondSpent);
        
        DailyBudget afterReset = budgetService.getDailyBudgetBy(memberProfile.getId(), today);
        assertThat(afterReset.getSpendAmount()).isEqualTo(BigDecimal.ZERO);
        assertThat(afterReset.getAvailableAmount()).isEqualTo(BigDecimal.valueOf(dailyLimit));
        assertThat(afterReset.isOverLimit()).isFalse();
    }

    @DisplayName("월별 예산 생성부터 지출 추가, 한도 변경까지의 전체 시나리오 테스트")
    @Test
    void monthlyBudgetCompleteScenario() {
        // given - 월별 예산 생성
        YearMonth currentMonth = YearMonth.now();
        Long monthlyLimit = 500000L;
        MonthlyBudget monthlyBudget = new MonthlyBudget(memberProfile, BigDecimal.valueOf(monthlyLimit), currentMonth);
        budgetRepository.save(monthlyBudget);

        // when & then - 1. 초기 상태 확인
        MonthlyBudget savedBudget = budgetService.getMonthlyBudgetBy(memberProfile.getId(), currentMonth);
        assertThat(savedBudget.getSpendAmount()).isEqualTo(BigDecimal.ZERO);
        assertThat(savedBudget.getAvailableAmount()).isEqualTo(BigDecimal.valueOf(monthlyLimit));
        assertThat(savedBudget.isOverLimit()).isFalse();

        // when & then - 2. 여러 번에 걸친 지출 추가
        savedBudget.addSpent(BigDecimal.valueOf(150000)); // 첫 번째 지출
        savedBudget.addSpent(200000); // 두 번째 지출 (int)
        savedBudget.addSpent(99999.99); // 세 번째 지출 (double)
        budgetRepository.save(savedBudget);
        
        MonthlyBudget afterSpending = budgetService.getMonthlyBudgetBy(memberProfile.getId(), currentMonth);
        assertThat(afterSpending.getSpendAmount()).isEqualTo(BigDecimal.valueOf(449999.99));
        assertThat(afterSpending.getAvailableAmount()).isEqualTo(BigDecimal.valueOf(50000.01));
        assertThat(afterSpending.isOverLimit()).isFalse();

        // when & then - 3. 한도 변경
        Long newLimit = 400000L;
        budgetService.editMonthlyBudgetCustom(memberProfile.getId(), currentMonth, newLimit);
        
        MonthlyBudget afterLimitChange = budgetService.getMonthlyBudgetBy(memberProfile.getId(), currentMonth);
        assertThat(afterLimitChange.getLimit()).isEqualTo(BigDecimal.valueOf(newLimit));
        assertThat(afterLimitChange.getSpendAmount()).isEqualTo(BigDecimal.valueOf(449999.99));
        assertThat(afterLimitChange.isOverLimit()).isTrue(); // 새 한도로 인해 초과 상태
    }

    @DisplayName("한 달간의 일일 예산을 디폴트로 생성하고 개별 수정하는 시나리오")
    @Test
    void monthlyDailyBudgetManagementScenario() {
        // given - 이번 달 1일부터 디폴트 일일 예산 생성
        LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
        Long defaultDailyLimit = 20000L;

        // when - 디폴트 일일 예산 생성
        budgetService.registerDefaultDailyBudgetBy(memberProfile.getId(), defaultDailyLimit, firstDayOfMonth);

        // then - 1. 모든 일일 예산이 생성되었는지 확인
        List<DailyBudget> allDailyBudgets = budgetRepository.findDailyBudgetsViaType(memberProfile.getId());
        int daysInMonth = firstDayOfMonth.lengthOfMonth();
        assertThat(allDailyBudgets).hasSize(daysInMonth);

        // when & then - 2. 특정 날짜의 예산 한도 개별 수정
        LocalDate specificDate = firstDayOfMonth.plusDays(10);
        Long newLimitForSpecificDate = 35000L;
        budgetService.editDailyBudgetCustom(memberProfile.getId(), specificDate, newLimitForSpecificDate);

        DailyBudget modifiedBudget = budgetService.getDailyBudgetBy(memberProfile.getId(), specificDate);
        assertThat(modifiedBudget.getLimit()).isEqualTo(BigDecimal.valueOf(newLimitForSpecificDate));

        // when & then - 3. 다른 날짜의 예산은 그대로인지 확인
        LocalDate anotherDate = firstDayOfMonth.plusDays(5);
        DailyBudget unchangedBudget = budgetService.getDailyBudgetBy(memberProfile.getId(), anotherDate);
        assertThat(unchangedBudget.getLimit()).isEqualTo(BigDecimal.valueOf(defaultDailyLimit));
    }

    @DisplayName("주간 예산 조회 및 주간별 지출 패턴 분석 시나리오")
    @Test
    void weeklyBudgetAnalysisScenario() {
        // given - 한 주간의 일일 예산 및 지출 설정
        LocalDate monday = LocalDate.of(2025, 6, 16); // 월요일
        Long dailyLimit = 25000L;

        for (int i = 0; i < 7; i++) {
            LocalDate date = monday.plusDays(i);
            DailyBudget dailyBudget = new DailyBudget(memberProfile, BigDecimal.valueOf(dailyLimit), date);
            
            // 요일별 다른 지출 패턴 설정
            if (i < 5) { // 평일 (월~금)
                dailyBudget.addSpent(15000 + i * 2000); // 점진적 증가
            } else { // 주말 (토~일)
                dailyBudget.addSpent(30000); // 주말 과소비
            }
            
            budgetRepository.save(dailyBudget);
        }

        // when - 주간 예산 조회 (수요일 기준)
        LocalDate wednesday = monday.plusDays(2);
        List<DailyBudget> weeklyBudgets = budgetService.getDailyBudgetsByWeek(memberProfile.getId(), wednesday);

        // then - 주간 예산 분석
        assertThat(weeklyBudgets).hasSize(7);

        // 평일 예산 검증
        for (int i = 0; i < 5; i++) {
            DailyBudget weekdayBudget = weeklyBudgets.get(i);
            assertThat(weekdayBudget.getSpendAmount()).isEqualTo(BigDecimal.valueOf(15000 + i * 2000));
            assertThat(weekdayBudget.isOverLimit()).isFalse();
        }

        // 주말 예산 검증 (한도 초과)
        for (int i = 5; i < 7; i++) {
            DailyBudget weekendBudget = weeklyBudgets.get(i);
            assertThat(weekendBudget.getSpendAmount()).isEqualTo(BigDecimal.valueOf(30000));
            assertThat(weekendBudget.isOverLimit()).isTrue();
        }

        // 주간 총 지출 계산
        BigDecimal totalWeeklySpent = weeklyBudgets.stream()
                .map(DailyBudget::getSpendAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal expectedTotal = BigDecimal.valueOf(
                15000 + 17000 + 19000 + 21000 + 23000 + 30000 + 30000); // 155,000원
        assertThat(totalWeeklySpent).isEqualTo(expectedTotal);
    }

    @DisplayName("다중 사용자 예산 격리 테스트")
    @Test
    void multiUserBudgetIsolationTest() {
        // given - 두 번째 사용자 생성
        Member secondMember = Member.builder()
                .email("second@test.com")
                .rawPassword("password123!")
                .build();
        memberRepository.save(secondMember);

        MemberProfile secondProfile = MemberProfile.builder()
                .nickName("secondUser")
                .member(secondMember)
                .build();
        memberProfileRepository.save(secondProfile);

        // when - 두 사용자 모두에게 같은 날짜의 예산 생성
        LocalDate sameDate = LocalDate.now();
        DailyBudget firstUserBudget = new DailyBudget(memberProfile, BigDecimal.valueOf(20000), sameDate);
        DailyBudget secondUserBudget = new DailyBudget(secondProfile, BigDecimal.valueOf(30000), sameDate);
        
        budgetRepository.save(firstUserBudget);
        budgetRepository.save(secondUserBudget);

        // when - 각 사용자별로 다른 지출 추가
        firstUserBudget.addSpent(15000);
        secondUserBudget.addSpent(25000);
        
        budgetRepository.save(firstUserBudget);
        budgetRepository.save(secondUserBudget);

        // then - 각 사용자의 예산이 독립적으로 관리되는지 확인
        DailyBudget retrievedFirstBudget = budgetService.getDailyBudgetBy(memberProfile.getId(), sameDate);
        DailyBudget retrievedSecondBudget = budgetService.getDailyBudgetBy(secondProfile.getId(), sameDate);

        assertThat(retrievedFirstBudget.getSpendAmount()).isEqualTo(BigDecimal.valueOf(15000));
        assertThat(retrievedFirstBudget.getLimit()).isEqualTo(BigDecimal.valueOf(20000));
        assertThat(retrievedFirstBudget.isOverLimit()).isFalse();

        assertThat(retrievedSecondBudget.getSpendAmount()).isEqualTo(BigDecimal.valueOf(25000));
        assertThat(retrievedSecondBudget.getLimit()).isEqualTo(BigDecimal.valueOf(30000));
        assertThat(retrievedSecondBudget.isOverLimit()).isFalse();
    }

    @DisplayName("예산 데이터 일관성 및 동시성 테스트")
    @Test
    void budgetDataConsistencyTest() {
        // given - 월별 예산과 여러 일일 예산 생성
        YearMonth currentMonth = YearMonth.now();
        MonthlyBudget monthlyBudget = new MonthlyBudget(memberProfile, BigDecimal.valueOf(600000), currentMonth);
        budgetRepository.save(monthlyBudget);

        LocalDate startDate = currentMonth.atDay(1);
        for (int i = 0; i < 10; i++) {
            LocalDate date = startDate.plusDays(i);
            DailyBudget dailyBudget = new DailyBudget(memberProfile, BigDecimal.valueOf(20000), date);
            budgetRepository.save(dailyBudget);
        }

        // when - 동시에 여러 예산에 지출 추가
        monthlyBudget.addSpent(100000);
        
        List<DailyBudget> dailyBudgets = budgetRepository.findDailyBudgetsViaType(memberProfile.getId());
        for (int i = 0; i < dailyBudgets.size(); i++) {
            dailyBudgets.get(i).addSpent(10000 + i * 1000);
        }

        // 모든 변경사항 저장
        budgetRepository.save(monthlyBudget);
        budgetRepository.saveAll(dailyBudgets);

        // then - 데이터 일관성 확인
        MonthlyBudget retrievedMonthly = budgetService.getMonthlyBudgetBy(memberProfile.getId(), currentMonth);
        assertThat(retrievedMonthly.getSpendAmount()).isEqualTo(BigDecimal.valueOf(100000));

        List<DailyBudget> retrievedDailies = budgetRepository.findDailyBudgetsViaType(memberProfile.getId());
        for (int i = 0; i < retrievedDailies.size(); i++) {
            assertThat(retrievedDailies.get(i).getSpendAmount())
                    .isEqualTo(BigDecimal.valueOf(10000 + i * 1000));
        }

        // 총 일일 지출과 월별 지출의 독립성 확인
        BigDecimal totalDailySpent = retrievedDailies.stream()
                .map(DailyBudget::getSpendAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 월별 예산과 일일 예산의 지출은 독립적으로 관리됨
        assertThat(retrievedMonthly.getSpendAmount()).isNotEqualTo(totalDailySpent);
    }
} 