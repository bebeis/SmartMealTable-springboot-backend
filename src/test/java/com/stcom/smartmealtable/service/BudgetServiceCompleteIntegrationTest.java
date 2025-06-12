package com.stcom.smartmealtable.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.stcom.smartmealtable.domain.Budget.DailyBudget;
import com.stcom.smartmealtable.domain.Budget.MonthlyBudget;
import com.stcom.smartmealtable.domain.member.Member;
import com.stcom.smartmealtable.domain.member.MemberProfile;
import com.stcom.smartmealtable.repository.BudgetRepository;
import com.stcom.smartmealtable.repository.MemberProfileRepository;
import com.stcom.smartmealtable.repository.MemberRepository;
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
class BudgetServiceCompleteIntegrationTest {

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
                .email("budget_complete_test@example.com")
                .rawPassword("password123!")
                .build();
        memberRepository.save(member);

        memberProfile = MemberProfile.builder()
                .nickName("완전예산테스터")
                .member(member)
                .build();
        memberProfileRepository.save(memberProfile);
    }

    @Test
    @DisplayName("특정 일자의 일간 예산을 조회할 수 있다")
    void getDailyBudgetBy() {
        // given
        LocalDate date = LocalDate.of(2025, 7, 15);
        DailyBudget dailyBudget = new DailyBudget(memberProfile, BigDecimal.valueOf(25000), date);
        budgetRepository.save(dailyBudget);

        // when
        DailyBudget result = budgetService.getDailyBudgetBy(memberProfile.getId(), date);

        // then
        assertThat(result.getDate()).isEqualTo(date);
        assertThat(result.getLimit()).isEqualTo(BigDecimal.valueOf(25000));
        assertThat(result.getMemberProfile().getId()).isEqualTo(memberProfile.getId());
    }

    @Test
    @DisplayName("존재하지 않는 일간 예산 조회 시 예외가 발생한다")
    void getDailyBudgetByNotFound() {
        // given
        LocalDate date = LocalDate.of(2025, 7, 15);

        // when & then
        assertThatThrownBy(() -> budgetService.getDailyBudgetBy(memberProfile.getId(), date))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예산이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("특정 월의 월간 예산을 조회할 수 있다")
    void getMonthlyBudgetBy() {
        // given
        YearMonth yearMonth = YearMonth.of(2025, 7);
        MonthlyBudget monthlyBudget = new MonthlyBudget(memberProfile, BigDecimal.valueOf(800000), yearMonth);
        budgetRepository.save(monthlyBudget);

        // when
        MonthlyBudget result = budgetService.getMonthlyBudgetBy(memberProfile.getId(), yearMonth);

        // then
        assertThat(result.getYearMonth()).isEqualTo(yearMonth);
        assertThat(result.getLimit()).isEqualTo(BigDecimal.valueOf(800000));
        assertThat(result.getMemberProfile().getId()).isEqualTo(memberProfile.getId());
    }

    @Test
    @DisplayName("존재하지 않는 월간 예산 조회 시 예외가 발생한다")
    void getMonthlyBudgetByNotFound() {
        // given
        YearMonth yearMonth = YearMonth.of(2025, 7);

        // when & then
        assertThatThrownBy(() -> budgetService.getMonthlyBudgetBy(memberProfile.getId(), yearMonth))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 프로필로 접근");
    }

    @Test
    @DisplayName("기본 일간 예산을 시작일부터 월말까지 자동 생성할 수 있다")
    void registerDefaultDailyBudgetBy() {
        // given
        LocalDate startDate = LocalDate.of(2025, 7, 15);
        Long dailyLimit = 20000L;

        // when
        budgetService.registerDefaultDailyBudgetBy(memberProfile.getId(), dailyLimit, startDate);

        // then
        List<DailyBudget> budgets = budgetRepository.findDailyBudgetsByMemberProfileIdAndDateBetween(
                memberProfile.getId(), 
                startDate, 
                startDate.withDayOfMonth(startDate.lengthOfMonth())
        );
        
        // 7월 15일부터 31일까지 17일간
        assertThat(budgets).hasSize(17);
        assertThat(budgets.get(0).getDate()).isEqualTo(startDate);
        assertThat(budgets.get(budgets.size() - 1).getDate()).isEqualTo(LocalDate.of(2025, 7, 31));
        assertThat(budgets.stream().allMatch(b -> b.getLimit().equals(BigDecimal.valueOf(dailyLimit)))).isTrue();
    }

    @Test
    @DisplayName("기본 월간 예산을 생성할 수 있다")
    void registerDefaultMonthlyBudgetBy() {
        // given
        YearMonth startYearMonth = YearMonth.of(2025, 8);
        Long monthlyLimit = 900000L;

        // when
        budgetService.registerDefaultMonthlyBudgetBy(memberProfile.getId(), monthlyLimit, startYearMonth);

        // then
        MonthlyBudget budget = budgetRepository.findMonthlyBudgetByMemberProfileIdAndYearMonth(
                memberProfile.getId(), startYearMonth
        ).orElseThrow();
        
        assertThat(budget.getYearMonth()).isEqualTo(startYearMonth);
        assertThat(budget.getLimit()).isEqualTo(BigDecimal.valueOf(monthlyLimit));
        assertThat(budget.getMemberProfile().getId()).isEqualTo(memberProfile.getId());
    }

    @Test
    @DisplayName("기본 예산 생성 시 존재하지 않는 프로필 ID로 시도하면 예외가 발생한다")
    void registerDefaultBudgetWithInvalidProfileId() {
        // given
        Long invalidProfileId = 99999L;
        LocalDate date = LocalDate.now();
        YearMonth yearMonth = YearMonth.now();

        // when & then
        assertThatThrownBy(() -> budgetService.registerDefaultDailyBudgetBy(invalidProfileId, 20000L, date))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 프로필로 접근");

        assertThatThrownBy(() -> budgetService.registerDefaultMonthlyBudgetBy(invalidProfileId, 800000L, yearMonth))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 프로필로 접근");
    }

    @Test
    @DisplayName("일간 예산 한도를 수정할 수 있다")
    void editDailyBudgetCustom() {
        // given
        LocalDate date = LocalDate.of(2025, 8, 10);
        DailyBudget originalBudget = new DailyBudget(memberProfile, BigDecimal.valueOf(20000), date);
        budgetRepository.save(originalBudget);
        
        Long newLimit = 35000L;

        // when
        budgetService.editDailyBudgetCustom(memberProfile.getId(), date, newLimit);

        // then
        DailyBudget updatedBudget = budgetRepository.findDailyBudgetByMemberProfileIdAndDate(
                memberProfile.getId(), date
        ).orElseThrow();
        
        assertThat(updatedBudget.getLimit()).isEqualTo(BigDecimal.valueOf(newLimit));
    }

    @Test
    @DisplayName("월간 예산 한도를 수정할 수 있다")
    void editMonthlyBudgetCustom() {
        // given
        YearMonth yearMonth = YearMonth.of(2025, 8);
        MonthlyBudget originalBudget = new MonthlyBudget(memberProfile, BigDecimal.valueOf(800000), yearMonth);
        budgetRepository.save(originalBudget);
        
        Long newLimit = 1200000L;

        // when
        budgetService.editMonthlyBudgetCustom(memberProfile.getId(), yearMonth, newLimit);

        // then
        MonthlyBudget updatedBudget = budgetRepository.findMonthlyBudgetByMemberProfileIdAndYearMonth(
                memberProfile.getId(), yearMonth
        ).orElseThrow();
        
        assertThat(updatedBudget.getLimit()).isEqualTo(BigDecimal.valueOf(newLimit));
    }

    @Test
    @DisplayName("존재하지 않는 예산 수정 시 예외가 발생한다")
    void editBudgetCustomNotFound() {
        // given
        LocalDate date = LocalDate.of(2025, 8, 10);
        YearMonth yearMonth = YearMonth.of(2025, 8);

        // when & then
        assertThatThrownBy(() -> budgetService.editDailyBudgetCustom(memberProfile.getId(), date, 30000L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 프로필로 접근");

        assertThatThrownBy(() -> budgetService.editMonthlyBudgetCustom(memberProfile.getId(), yearMonth, 1000000L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 프로필로 접근");
    }

    @Test
    @DisplayName("사용자 정의 일간 예산을 저장할 수 있다")
    void saveDailyBudgetCustom() {
        // given
        Long dailyLimit = 30000L;

        // when
        budgetService.saveDailyBudgetCustom(memberProfile.getId(), dailyLimit);

        // then
        // 오늘 날짜로 예산이 생성되는지 확인
        DailyBudget savedBudget = budgetRepository.findDailyBudgetByMemberProfileIdAndDate(
                memberProfile.getId(), LocalDate.now()
        ).orElseThrow();
        
        assertThat(savedBudget.getLimit()).isEqualTo(BigDecimal.valueOf(dailyLimit));
        assertThat(savedBudget.getDate()).isEqualTo(LocalDate.now());
    }

    @Test
    @DisplayName("사용자 정의 월간 예산을 저장할 수 있다")
    void saveMonthlyBudgetCustom() {
        // given
        Long monthlyLimit = 900000L;

        // when
        budgetService.saveMonthlyBudgetCustom(memberProfile.getId(), monthlyLimit);

        // then
        // 이번 달로 예산이 생성되는지 확인
        MonthlyBudget savedBudget = budgetRepository.findMonthlyBudgetByMemberProfileIdAndYearMonth(
                memberProfile.getId(), YearMonth.now()
        ).orElseThrow();
        
        assertThat(savedBudget.getLimit()).isEqualTo(BigDecimal.valueOf(monthlyLimit));
        assertThat(savedBudget.getYearMonth()).isEqualTo(YearMonth.now());
    }

    @Test
    @DisplayName("사용자 정의 예산 저장 시 존재하지 않는 프로필 ID로 시도하면 예외가 발생한다")
    void saveCustomBudgetWithInvalidProfileId() {
        // given
        Long invalidProfileId = 99999L;

        // when & then
        assertThatThrownBy(() -> budgetService.saveDailyBudgetCustom(invalidProfileId, 25000L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 프로필로 접근");

        assertThatThrownBy(() -> budgetService.saveMonthlyBudgetCustom(invalidProfileId, 800000L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 프로필로 접근");
    }
} 