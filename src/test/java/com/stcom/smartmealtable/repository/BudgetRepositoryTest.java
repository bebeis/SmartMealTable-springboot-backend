package com.stcom.smartmealtable.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.stcom.smartmealtable.domain.Budget.DailyBudget;
import com.stcom.smartmealtable.domain.Budget.MonthlyBudget;
import com.stcom.smartmealtable.domain.member.Member;
import com.stcom.smartmealtable.domain.member.MemberProfile;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class BudgetRepositoryTest {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private MemberProfileRepository memberProfileRepository;

    @Autowired
    private MemberRepository memberRepository;

    @DisplayName("프로필 ID와 일자 정보로 일일 예산을 조회한다.")
    @Test
    void findDailyBudgetByMemberProfileIdAndDate() throws Exception {
        // given
        Member member = Member.builder()
                .email("abcd@naver.com")
                .rawPassword("@absdv123")
                .build();

        memberRepository.save(member);

        MemberProfile memberProfile = MemberProfile.builder()
                .nickName("testUser")
                .member(member)
                .build();
        memberProfileRepository.save(memberProfile);

        DailyBudget dailyBudget = new DailyBudget(memberProfile, BigDecimal.valueOf(1000), LocalDate.now());
        budgetRepository.save(dailyBudget);

        // when
        DailyBudget foundBudget = budgetRepository.findDailyBudgetByMemberProfileIdAndDate(
                        memberProfile.getId(), LocalDate.now())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예산입니다"));

        // then
        assertThat(foundBudget).isNotNull();
        assertThat(foundBudget.getMemberProfile()).isEqualTo(memberProfile);
        assertThat(foundBudget.getLimit()).isEqualByComparingTo(BigDecimal.valueOf(1000));
        assertThat(foundBudget.getDate()).isEqualTo(LocalDate.now());
        assertThat(foundBudget.getId()).isNotNull();
    }

    @DisplayName("존재하지 않는 프로필 ID와 일자 정보로 일일 예산을 조회하면 빈 Optional을 반환한다.")
    @Test
    void findDailyBudgetByMemberProfileIdAndDate2() throws Exception {
        // given
        Member member = Member.builder()
                .email("abcd@naver.com")
                .rawPassword("@absdv123")
                .build();

        memberRepository.save(member);

        MemberProfile memberProfile = MemberProfile.builder()
                .nickName("testUser")
                .member(member)
                .build();
        memberProfileRepository.save(memberProfile);

        LocalDate date = LocalDate.now();
        DailyBudget dailyBudget = new DailyBudget(memberProfile, BigDecimal.valueOf(1000), date);
        budgetRepository.save(dailyBudget);

        // when
        // 존재하지 않는 프로필 ID와 일자 정보로 조회
        Long nonExistentProfileId = 999L;
        var foundBudget = budgetRepository.findDailyBudgetByMemberProfileIdAndDate(nonExistentProfileId, date);

        // then
        assertThat(foundBudget).isEmpty();
    }

    @DisplayName("프로필 ID와 년월 정보로 월별 예산을 조회한다")
    @Test
    void findMonthlyBudgetByMemberProfileIdAndYearMonth() throws Exception {
        // given
        // given
        Member member = Member.builder()
                .email("abcd@naver.com")
                .rawPassword("@absdv123")
                .build();

        memberRepository.save(member);

        MemberProfile memberProfile = MemberProfile.builder()
                .nickName("testUser")
                .member(member)
                .build();
        memberProfileRepository.save(memberProfile);

        YearMonth yearMonth = YearMonth.now();
        MonthlyBudget monthlyBudget = new MonthlyBudget(memberProfile, BigDecimal.valueOf(3000), yearMonth);
        budgetRepository.save(monthlyBudget);

        // when
        MonthlyBudget foundBudget = budgetRepository.findMonthlyBudgetByMemberProfileIdAndYearMonth(
                        memberProfile.getId(), yearMonth)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예산입니다"));

        // then
        assertThat(foundBudget).isNotNull();

    }

    @DisplayName("주어진 날짜 기간의 예산 정보를 조회한다")
    @Test
    void findDailyBudgetsByMemberProfileIdAndDateBetween() throws Exception {
        // given
        Member member = Member.builder()
                .email("abcd@naver.com")
                .rawPassword("@absdv123")
                .build();

        memberRepository.save(member);

        MemberProfile memberProfile = MemberProfile.builder()
                .nickName("testUser")
                .member(member)
                .build();
        memberProfileRepository.save(memberProfile);

        // when
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.minusDays(3); // 3일 전
        LocalDate endOfWeek = today.plusDays(3); // 3일 후

        // 예시로 5일의 일일 예산을 생성
        for (int i = 0; i < 5; i++) {
            LocalDate date = today.minusDays(i);
            DailyBudget dailyBudget = new DailyBudget(memberProfile, BigDecimal.valueOf(1000 + i * 100), date);
            budgetRepository.save(dailyBudget);
        }

        // 주어진 날짜 범위 내의 일일 예산을 조회
        var dailyBudgets = budgetRepository.findDailyBudgetsByMemberProfileIdAndDateBetween(
                memberProfile.getId(), startOfWeek, endOfWeek);

        // then
        assertThat(dailyBudgets).isNotEmpty();
        assertThat(dailyBudgets.size()).isEqualTo(4);

    }
}