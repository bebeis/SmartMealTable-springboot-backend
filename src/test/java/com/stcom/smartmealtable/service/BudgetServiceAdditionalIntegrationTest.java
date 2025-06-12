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
class BudgetServiceAdditionalIntegrationTest {

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
                .email("budget_test@example.com")
                .rawPassword("password123!")
                .build();
        memberRepository.save(member);

        memberProfile = MemberProfile.builder()
                .nickName("budgetUser")
                .member(member)
                .build();
        memberProfileRepository.save(memberProfile);
    }

    @Test
    @DisplayName("존재하지 않는 프로필로 월간 예산을 조회하면 예외가 발생한다")
    void findRecentMonthlyBudgetWithInvalidProfileId() {
        // given
        Long invalidProfileId = 9999L;

        // when & then
        assertThatThrownBy(() -> budgetService.findRecentMonthlyBudgetByMemberProfileId(invalidProfileId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 프로필로 접근");
    }

    @Test
    @DisplayName("존재하지 않는 프로필로 일일 예산을 조회하면 예외가 발생한다")
    void findRecentDailyBudgetWithInvalidProfileId() {
        // given
        Long invalidProfileId = 9999L;

        // when & then
        assertThatThrownBy(() -> budgetService.findRecentDailyBudgetByMemberProfileId(invalidProfileId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 프로필로 접근");
    }

    @Test
    @DisplayName("존재하지 않는 프로필로 월간 예산을 저장하면 예외가 발생한다")
    void saveMonthlyBudgetWithInvalidProfileId() {
        // given
        Long invalidProfileId = 9999L;

        // when & then
        assertThatThrownBy(() -> budgetService.saveMonthlyBudgetCustom(invalidProfileId, 100000L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 프로필로 접근");
    }

    @Test
    @DisplayName("존재하지 않는 프로필로 일일 예산을 저장하면 예외가 발생한다")
    void saveDailyBudgetWithInvalidProfileId() {
        // given
        Long invalidProfileId = 9999L;

        // when & then
        assertThatThrownBy(() -> budgetService.saveDailyBudgetCustom(invalidProfileId, 10000L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 프로필로 접근");
    }

    @Test
    @DisplayName("존재하지 않는 프로필로 기본 일일 예산을 등록하면 예외가 발생한다")
    void registerDefaultDailyBudgetWithInvalidProfileId() {
        // given
        Long invalidProfileId = 9999L;
        LocalDate startDate = LocalDate.of(2025, 6, 15);

        // when & then
        assertThatThrownBy(() -> budgetService.registerDefaultDailyBudgetBy(invalidProfileId, 10000L, startDate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 프로필로 접근");
    }

    @Test
    @DisplayName("존재하지 않는 프로필로 기본 월간 예산을 등록하면 예외가 발생한다")
    void registerDefaultMonthlyBudgetWithInvalidProfileId() {
        // given
        Long invalidProfileId = 9999L;
        YearMonth yearMonth = YearMonth.of(2025, 6);

        // when & then
        assertThatThrownBy(() -> budgetService.registerDefaultMonthlyBudgetBy(invalidProfileId, 500000L, yearMonth))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 프로필로 접근");
    }

    @Test
    @DisplayName("일일 예산과 월간 예산이 둘 다 존재하는 경우 정상 조회된다")
    void findBothDailyAndMonthlyBudgets() {
        // given
        Long dailyLimit = 10000L;
        Long monthlyLimit = 300000L;
        LocalDate today = LocalDate.of(2025, 6, 15);
        YearMonth thisMonth = YearMonth.of(2025, 6);

        // 예산 생성
        DailyBudget dailyBudget = new DailyBudget(memberProfile, BigDecimal.valueOf(dailyLimit), today);
        MonthlyBudget monthlyBudget = new MonthlyBudget(memberProfile, BigDecimal.valueOf(monthlyLimit), thisMonth);
        budgetRepository.saveAll(List.of(dailyBudget, monthlyBudget));

        // when
        DailyBudget foundDaily = budgetService.getDailyBudgetBy(memberProfile.getId(), today);
        MonthlyBudget foundMonthly = budgetService.getMonthlyBudgetBy(memberProfile.getId(), thisMonth);

        // then
        assertThat(foundDaily.getLimit()).isEqualTo(BigDecimal.valueOf(dailyLimit));
        assertThat(foundMonthly.getLimit()).isEqualTo(BigDecimal.valueOf(monthlyLimit));
    }

    @Test
    @DisplayName("존재하지 않는 날짜의 일일 예산을 수정하려고 하면 예외가 발생한다")
    void editDailyBudgetWithNonExistentDate() {
        // given
        LocalDate nonExistentDate = LocalDate.of(2030, 12, 31);

        // when & then
        assertThatThrownBy(() -> budgetService.editDailyBudgetCustom(memberProfile.getId(), nonExistentDate, 15000L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 프로필로 접근");
    }

    @Test
    @DisplayName("존재하지 않는 년월의 월간 예산을 수정하려고 하면 예외가 발생한다")
    void editMonthlyBudgetWithNonExistentYearMonth() {
        // given
        YearMonth nonExistentYearMonth = YearMonth.of(2030, 12);

        // when & then
        assertThatThrownBy(() -> budgetService.editMonthlyBudgetCustom(memberProfile.getId(), nonExistentYearMonth, 500000L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 프로필로 접근");
    }
} 