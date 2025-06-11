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
class BudgetServiceIntegrationTest {

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
                .email("test@example.com")
                .rawPassword("password123!")
                .build();
        memberRepository.save(member);

        memberProfile = MemberProfile.builder()
                .nickName("testUser")
                .member(member)
                .build();
        memberProfileRepository.save(memberProfile);
    }

    @DisplayName("일일 예산을 저장하고 조회할 수 있다")
    @Test
    void saveDailyBudgetCustom() {
        // given
        Long limit = 50000L;

        // when
        budgetService.saveDailyBudgetCustom(memberProfile.getId(), limit);

        // then
        DailyBudget savedBudget = budgetService.findRecentDailyBudgetByMemberProfileId(memberProfile.getId());
        assertThat(savedBudget.getLimit()).isEqualTo(BigDecimal.valueOf(limit));
        assertThat(savedBudget.getMemberProfile().getId()).isEqualTo(memberProfile.getId());
        assertThat(savedBudget.getSpendAmount()).isEqualTo(BigDecimal.ZERO);
    }

    @DisplayName("월별 예산을 저장하고 조회할 수 있다")
    @Test
    void saveMonthlyBudgetCustom() {
        // given
        Long limit = 1000000L;

        // when
        budgetService.saveMonthlyBudgetCustom(memberProfile.getId(), limit);

        // then
        MonthlyBudget savedBudget = budgetService.findRecentMonthlyBudgetByMemberProfileId(memberProfile.getId());
        assertThat(savedBudget.getLimit()).isEqualTo(BigDecimal.valueOf(limit));
        assertThat(savedBudget.getMemberProfile().getId()).isEqualTo(memberProfile.getId());
        assertThat(savedBudget.getSpendAmount()).isEqualTo(BigDecimal.ZERO);
    }

    @DisplayName("특정 날짜의 일일 예산을 조회할 수 있다")
    @Test
    void getDailyBudgetByDate() {
        // given
        LocalDate targetDate = LocalDate.of(2025, 6, 15);
        DailyBudget dailyBudget = new DailyBudget(memberProfile, BigDecimal.valueOf(30000), targetDate);
        budgetRepository.save(dailyBudget);

        // when
        DailyBudget foundBudget = budgetService.getDailyBudgetBy(memberProfile.getId(), targetDate);

        // then
        assertThat(foundBudget.getDate()).isEqualTo(targetDate);
        assertThat(foundBudget.getLimit()).isEqualTo(BigDecimal.valueOf(30000));
        assertThat(foundBudget.getMemberProfile().getId()).isEqualTo(memberProfile.getId());
    }

    @DisplayName("특정 년월의 월별 예산을 조회할 수 있다")
    @Test
    void getMonthlyBudgetByYearMonth() {
        // given
        YearMonth targetYearMonth = YearMonth.of(2025, 6);
        MonthlyBudget monthlyBudget = new MonthlyBudget(memberProfile, BigDecimal.valueOf(800000), targetYearMonth);
        budgetRepository.save(monthlyBudget);

        // when
        MonthlyBudget foundBudget = budgetService.getMonthlyBudgetBy(memberProfile.getId(), targetYearMonth);

        // then
        assertThat(foundBudget.getYearMonth()).isEqualTo(targetYearMonth);
        assertThat(foundBudget.getLimit()).isEqualTo(BigDecimal.valueOf(800000));
        assertThat(foundBudget.getMemberProfile().getId()).isEqualTo(memberProfile.getId());
    }

    @DisplayName("한 주간의 일일 예산 목록을 조회할 수 있다")
    @Test
    void getDailyBudgetsByWeek() {
        // given
        LocalDate monday = LocalDate.of(2025, 6, 9); // 월요일

        // 월요일부터 일요일까지 일일 예산 생성
        for (int i = 0; i < 7; i++) {
            LocalDate date = monday.plusDays(i);
            DailyBudget dailyBudget = new DailyBudget(memberProfile, BigDecimal.valueOf(10000 + i * 1000), date);
            budgetRepository.save(dailyBudget);
        }

        // when - 주 중 아무 날짜나 입력해도 해당 주의 예산을 조회
        LocalDate wednesday = monday.plusDays(2);
        List<DailyBudget> weeklyBudgets = budgetService.getDailyBudgetsByWeek(memberProfile.getId(), wednesday);

        // then
        assertThat(weeklyBudgets).hasSize(7);
        assertThat(weeklyBudgets.get(0).getDate()).isEqualTo(monday);
        assertThat(weeklyBudgets.get(6).getDate()).isEqualTo(monday.plusDays(6));
    }

    @DisplayName("디폴트 일일 예산을 등록하면 해당 월의 남은 일자에 대해 일일 예산이 생성된다")
    @Test
    void registerDefaultDailyBudget() {
        // given
        Long dailyLimit = 25000L;
        LocalDate startDate = LocalDate.of(2025, 6, 15); // 6월 15일부터

        // when
        budgetService.registerDefaultDailyBudgetBy(memberProfile.getId(), dailyLimit, startDate);

        // then
        // 6월 15일부터 6월 30일까지 16개의 일일 예산이 생성되어야 함
        List<DailyBudget> dailyBudgets = budgetRepository.findDailyBudgetsViaType(memberProfile.getId());
        assertThat(dailyBudgets).hasSize(16);

        for (DailyBudget budget : dailyBudgets) {
            assertThat(budget.getLimit()).isEqualTo(BigDecimal.valueOf(dailyLimit));
            assertThat(budget.getDate()).isBetween(startDate, LocalDate.of(2025, 6, 30));
        }
    }

    @DisplayName("디폴트 월별 예산을 등록할 수 있다")
    @Test
    void registerDefaultMonthlyBudget() {
        // given
        Long monthlyLimit = 500000L;
        YearMonth targetYearMonth = YearMonth.of(2025, 7);

        // when
        budgetService.registerDefaultMonthlyBudgetBy(memberProfile.getId(), monthlyLimit, targetYearMonth);

        // then
        MonthlyBudget savedBudget = budgetService.getMonthlyBudgetBy(memberProfile.getId(), targetYearMonth);
        assertThat(savedBudget.getLimit()).isEqualTo(BigDecimal.valueOf(monthlyLimit));
        assertThat(savedBudget.getYearMonth()).isEqualTo(targetYearMonth);
    }

    @DisplayName("일일 예산 한도를 수정할 수 있다")
    @Test
    void editDailyBudgetCustom() {
        // given
        LocalDate targetDate = LocalDate.of(2025, 6, 20);
        DailyBudget dailyBudget = new DailyBudget(memberProfile, BigDecimal.valueOf(20000), targetDate);
        budgetRepository.save(dailyBudget);

        Long newLimit = 35000L;

        // when
        budgetService.editDailyBudgetCustom(memberProfile.getId(), targetDate, newLimit);

        // then
        DailyBudget updatedBudget = budgetService.getDailyBudgetBy(memberProfile.getId(), targetDate);
        assertThat(updatedBudget.getLimit()).isEqualTo(BigDecimal.valueOf(newLimit));
    }

    @DisplayName("월별 예산 한도를 수정할 수 있다")
    @Test
    void editMonthlyBudgetCustom() {
        // given
        YearMonth targetYearMonth = YearMonth.of(2025, 8);
        MonthlyBudget monthlyBudget = new MonthlyBudget(memberProfile, BigDecimal.valueOf(600000), targetYearMonth);
        budgetRepository.save(monthlyBudget);

        Long newLimit = 750000L;

        // when
        budgetService.editMonthlyBudgetCustom(memberProfile.getId(), targetYearMonth, newLimit);

        // then
        MonthlyBudget updatedBudget = budgetService.getMonthlyBudgetBy(memberProfile.getId(), targetYearMonth);
        assertThat(updatedBudget.getLimit()).isEqualTo(BigDecimal.valueOf(newLimit));
    }

    @DisplayName("존재하지 않는 프로필 ID로 예산을 조회하면 예외가 발생한다")
    @Test
    void getBudgetWithInvalidProfileId() {
        // given
        Long invalidProfileId = 999L;
        LocalDate targetDate = LocalDate.now();

        // when & then
        assertThatThrownBy(() -> budgetService.getDailyBudgetBy(invalidProfileId, targetDate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예산이 존재하지 않습니다.");
    }

    @DisplayName("존재하지 않는 날짜의 예산을 조회하면 예외가 발생한다")
    @Test
    void getBudgetWithInvalidDate() {
        // given
        LocalDate nonExistentDate = LocalDate.of(2030, 12, 31);

        // when & then
        assertThatThrownBy(() -> budgetService.getDailyBudgetBy(memberProfile.getId(), nonExistentDate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예산이 존재하지 않습니다.");
    }

    @DisplayName("존재하지 않는 프로필 ID로 예산을 등록하면 예외가 발생한다")
    @Test
    void saveBudgetWithInvalidProfileId() {
        // given
        Long invalidProfileId = 999L;
        Long limit = 50000L;

        // when & then
        assertThatThrownBy(() -> budgetService.saveDailyBudgetCustom(invalidProfileId, limit))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 프로필로 접근");
    }
} 