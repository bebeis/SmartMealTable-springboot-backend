package com.stcom.smartmealtable.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stcom.smartmealtable.domain.Budget.DailyBudget;
import com.stcom.smartmealtable.domain.Budget.MonthlyBudget;
import com.stcom.smartmealtable.domain.member.MemberProfile;
import com.stcom.smartmealtable.repository.BudgetRepository;
import com.stcom.smartmealtable.repository.MemberProfileRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @InjectMocks
    private BudgetService budgetService;

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private MemberProfileRepository memberProfileRepository;

    @Test
    @DisplayName("회원 프로필 ID로 최근 일일 예산을 조회할 수 있다")
    void findRecentDailyBudgetByMemberProfileId() {
        // given
        Long memberProfileId = 1L;
        MemberProfile memberProfile = new MemberProfile();
        DailyBudget dailyBudget = new DailyBudget(memberProfile, BigDecimal.valueOf(10000), LocalDate.now());
        
        when(budgetRepository.findFirstDailyBudgetByMemberProfileId(memberProfileId))
            .thenReturn(Optional.of(dailyBudget));

        // when
        DailyBudget result = budgetService.findRecentDailyBudgetByMemberProfileId(memberProfileId);

        // then
        assertThat(result).isEqualTo(dailyBudget);
        assertThat(result.getLimit()).isEqualTo(BigDecimal.valueOf(10000));
    }

    @Test
    @DisplayName("존재하지 않는 회원 프로필 ID로 일일 예산을 조회하면 예외가 발생한다")
    void findRecentDailyBudgetByMemberProfileId_NotFound() {
        // given
        Long memberProfileId = 999L;
        
        when(budgetRepository.findFirstDailyBudgetByMemberProfileId(memberProfileId))
            .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> budgetService.findRecentDailyBudgetByMemberProfileId(memberProfileId))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("존재하지 않는 프로필로 접근");
    }

    @Test
    @DisplayName("회원 프로필 ID로 최근 월별 예산을 조회할 수 있다")
    void findRecentMonthlyBudgetByMemberProfileId() {
        // given
        Long memberProfileId = 1L;
        MemberProfile memberProfile = new MemberProfile();
        MonthlyBudget monthlyBudget = new MonthlyBudget(memberProfile, BigDecimal.valueOf(300000), YearMonth.now());
        
        when(budgetRepository.findFirstMonthlyBudgetByMemberProfileId(memberProfileId))
            .thenReturn(Optional.of(monthlyBudget));

        // when
        MonthlyBudget result = budgetService.findRecentMonthlyBudgetByMemberProfileId(memberProfileId);

        // then
        assertThat(result).isEqualTo(monthlyBudget);
        assertThat(result.getLimit()).isEqualTo(BigDecimal.valueOf(300000));
    }

    @Test
    @DisplayName("월별 예산을 저장할 수 있다")
    void saveMonthlyBudgetCustom() {
        // given
        Long memberProfileId = 1L;
        Long limit = 300000L;
        MemberProfile memberProfile = new MemberProfile();
        
        when(memberProfileRepository.findById(memberProfileId))
            .thenReturn(Optional.of(memberProfile));

        // when
        budgetService.saveMonthlyBudgetCustom(memberProfileId, limit);

        // then
        verify(budgetRepository).save(any(MonthlyBudget.class));
    }

    @Test
    @DisplayName("일일 예산을 저장할 수 있다")
    void saveDailyBudgetCustom() {
        // given
        Long memberProfileId = 1L;
        Long limit = 10000L;
        MemberProfile memberProfile = new MemberProfile();
        
        when(memberProfileRepository.findById(memberProfileId))
            .thenReturn(Optional.of(memberProfile));

        // when
        budgetService.saveDailyBudgetCustom(memberProfileId, limit);

        // then
        verify(budgetRepository).save(any(DailyBudget.class));
    }

    @Test
    @DisplayName("존재하지 않는 회원 프로필 ID로 예산을 저장하면 예외가 발생한다")
    void saveBudget_NotFound() {
        // given
        Long memberProfileId = 999L;
        Long limit = 10000L;
        
        when(memberProfileRepository.findById(memberProfileId))
            .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> budgetService.saveDailyBudgetCustom(memberProfileId, limit))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("존재하지 않는 프로필로 접근");
    }
} 