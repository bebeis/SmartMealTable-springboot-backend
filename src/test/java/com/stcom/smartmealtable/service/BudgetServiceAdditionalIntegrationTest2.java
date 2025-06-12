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
import java.time.DayOfWeek;
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
class BudgetServiceAdditionalIntegrationTest2 {

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
                .email("budget_test2@example.com")
                .rawPassword("password123!")
                .build();
        memberRepository.save(member);

        memberProfile = MemberProfile.builder()
                .nickName("예산테스터2")
                .member(member)
                .build();
        memberProfileRepository.save(memberProfile);
    }

    @Test
    @DisplayName("주간 예산 조회 시 월요일부터 일요일까지의 예산이 모두 조회된다")
    void getDailyBudgetsByWeek() {
        // given
        // 특정 주의 월요일 구하기
        LocalDate today = LocalDate.of(2025, 7, 16); // 수요일
        LocalDate monday = today.with(DayOfWeek.MONDAY);
        
        // 해당 주의 모든 일자에 예산 생성 (월~일)
        for (int i = 0; i < 7; i++) {
            LocalDate date = monday.plusDays(i);
            // 요일별로 다른 금액 설정
            DailyBudget dailyBudget = new DailyBudget(
                    memberProfile, 
                    BigDecimal.valueOf(10000 + i * 1000), 
                    date);
            budgetRepository.save(dailyBudget);
        }

        // when - 해당 주의 중간 날짜(수요일)로 조회해도 월요일부터 일요일까지 모두 조회되어야 함
        List<DailyBudget> weekBudgets = budgetService.getDailyBudgetsByWeek(memberProfile.getId(), today);

        // then
        assertThat(weekBudgets).hasSize(7);
        assertThat(weekBudgets.get(0).getDate()).isEqualTo(monday); // 첫 번째는 월요일
        assertThat(weekBudgets.get(6).getDate()).isEqualTo(monday.plusDays(6)); // 마지막은 일요일
        
        // 날짜순으로 정렬되어 있는지 확인
        for (int i = 0; i < weekBudgets.size(); i++) {
            assertThat(weekBudgets.get(i).getDate()).isEqualTo(monday.plusDays(i));
            // 금액 확인 (10000 + i * 1000)
            assertThat(weekBudgets.get(i).getLimit()).isEqualTo(BigDecimal.valueOf(10000 + i * 1000));
        }
    }
    
    @Test
    @DisplayName("빈 주간 예산 조회 시 빈 리스트가 반환된다")
    void getDailyBudgetsByWeekWhenEmpty() {
        // given
        LocalDate date = LocalDate.of(2026, 1, 1);
        
        // when
        List<DailyBudget> weekBudgets = budgetService.getDailyBudgetsByWeek(memberProfile.getId(), date);
        
        // then
        assertThat(weekBudgets).isEmpty();
    }
    
    @Test
    @DisplayName("일부 날짜만 예산이 있는 주간 조회 시 존재하는 날짜의 예산만 조회된다")
    void getDailyBudgetsByWeekWithPartialData() {
        // given
        LocalDate monday = LocalDate.of(2025, 8, 4);
        LocalDate wednesday = monday.plusDays(2);
        LocalDate friday = monday.plusDays(4);
        
        // 수요일과 금요일에만 예산 설정
        DailyBudget wednesdayBudget = new DailyBudget(memberProfile, BigDecimal.valueOf(15000), wednesday);
        DailyBudget fridayBudget = new DailyBudget(memberProfile, BigDecimal.valueOf(20000), friday);
        budgetRepository.saveAll(List.of(wednesdayBudget, fridayBudget));
        
        // when
        List<DailyBudget> weekBudgets = budgetService.getDailyBudgetsByWeek(memberProfile.getId(), monday);
        
        // then
        assertThat(weekBudgets).hasSize(2); // 수요일, 금요일 두 개만 있어야 함
        assertThat(weekBudgets.stream().map(DailyBudget::getDate))
                .containsExactlyInAnyOrder(wednesday, friday);
    }
    
    @Test
    @DisplayName("존재하지 않는 프로필 ID로 주간 예산 조회 시 빈 리스트가 반환된다")
    void getDailyBudgetsByWeekWithInvalidProfileId() {
        // given
        Long invalidProfileId = 99999L;
        LocalDate date = LocalDate.now();
        
        // when
        List<DailyBudget> weekBudgets = budgetService.getDailyBudgetsByWeek(invalidProfileId, date);
        
        // then
        assertThat(weekBudgets).isEmpty();
    }
} 