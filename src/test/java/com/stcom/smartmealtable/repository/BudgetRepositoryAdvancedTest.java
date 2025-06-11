package com.stcom.smartmealtable.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.stcom.smartmealtable.domain.Budget.DailyBudget;
import com.stcom.smartmealtable.domain.Budget.MonthlyBudget;
import com.stcom.smartmealtable.domain.member.Member;
import com.stcom.smartmealtable.domain.member.MemberProfile;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class BudgetRepositoryAdvancedTest {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private MemberProfileRepository memberProfileRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Member member1, member2;
    private MemberProfile profile1, profile2;

    @BeforeEach
    void setUp() {
        // 첫 번째 멤버 및 프로필
        member1 = Member.builder()
                .email("user1@test.com")
                .rawPassword("password123!")
                .build();
        memberRepository.save(member1);

        profile1 = MemberProfile.builder()
                .nickName("user1")
                .member(member1)
                .build();
        memberProfileRepository.save(profile1);

        // 두 번째 멤버 및 프로필
        member2 = Member.builder()
                .email("user2@test.com")
                .rawPassword("password123!")
                .build();
        memberRepository.save(member2);

        profile2 = MemberProfile.builder()
                .nickName("user2")
                .member(member2)
                .build();
        memberProfileRepository.save(profile2);
    }

    @DisplayName("타입별 예산 조회 쿼리가 올바르게 동작한다")
    @Test
    void findBudgetsByTypeQueryTest() {
        // given - 다양한 타입의 예산 생성
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.now();

        // 일일 예산들
        for (int i = 0; i < 5; i++) {
            DailyBudget dailyBudget = new DailyBudget(profile1, BigDecimal.valueOf(20000 + i * 1000), today.plusDays(i));
            budgetRepository.save(dailyBudget);
        }

        // 월별 예산들
        for (int i = 0; i < 3; i++) {
            MonthlyBudget monthlyBudget = new MonthlyBudget(profile1, BigDecimal.valueOf(500000 + i * 100000), currentMonth.plusMonths(i));
            budgetRepository.save(monthlyBudget);
        }

        // 다른 사용자의 예산들 (격리 테스트용)
        DailyBudget otherUserDaily = new DailyBudget(profile2, BigDecimal.valueOf(30000), today);
        budgetRepository.save(otherUserDaily);

        // when
        List<DailyBudget> dailyBudgets = budgetRepository.findDailyBudgetsViaType(profile1.getId());
        List<MonthlyBudget> monthlyBudgets = budgetRepository.findMonthlyBudgetsViaType(profile1.getId());

        // then
        assertThat(dailyBudgets).hasSize(5);
        assertThat(monthlyBudgets).hasSize(3);

        // 타입 검증
        for (DailyBudget budget : dailyBudgets) {
            assertThat(budget).isInstanceOf(DailyBudget.class);
            assertThat(budget.getMemberProfile().getId()).isEqualTo(profile1.getId());
        }

        for (MonthlyBudget budget : monthlyBudgets) {
            assertThat(budget).isInstanceOf(MonthlyBudget.class);
            assertThat(budget.getMemberProfile().getId()).isEqualTo(profile1.getId());
        }
    }

    @DisplayName("최신 예산 조회 쿼리가 올바르게 정렬하여 조회한다")
    @Test
    void findFirstBudgetOrderingTest() {
        // given - 고유한 프로필로 테스트
        Member testMember = Member.builder()
                .email("ordering@test.com")
                .rawPassword("password123!")
                .build();
        memberRepository.save(testMember);

        MemberProfile testProfile = MemberProfile.builder()
                .nickName("orderingTestUser")
                .member(testMember)
                .build();
        memberProfileRepository.save(testProfile);

        // 날짜 순서가 뒤섞인 예산들 생성
        LocalDate baseDate = LocalDate.of(2025, 8, 15);
        YearMonth baseMonth = YearMonth.of(2025, 8);

        // 일일 예산들 (날짜 순서 뒤섞어서 생성)
        DailyBudget dailyBudget1 = new DailyBudget(testProfile, BigDecimal.valueOf(20000), baseDate.plusDays(5));
        DailyBudget dailyBudget2 = new DailyBudget(testProfile, BigDecimal.valueOf(25000), baseDate.plusDays(2));
        DailyBudget dailyBudget3 = new DailyBudget(testProfile, BigDecimal.valueOf(30000), baseDate.plusDays(10)); // 가장 최신

        budgetRepository.save(dailyBudget1);
        budgetRepository.save(dailyBudget2);
        budgetRepository.save(dailyBudget3);

        // 월별 예산들 - 각각 다른 년월로 생성
        MonthlyBudget monthlyBudget1 = new MonthlyBudget(testProfile, BigDecimal.valueOf(500000), baseMonth.plusMonths(1));
        MonthlyBudget monthlyBudget2 = new MonthlyBudget(testProfile, BigDecimal.valueOf(600000), baseMonth.plusMonths(3)); // 가장 최신
        MonthlyBudget monthlyBudget3 = new MonthlyBudget(testProfile, BigDecimal.valueOf(550000), baseMonth.plusMonths(2));

        budgetRepository.save(monthlyBudget1);
        budgetRepository.save(monthlyBudget2);
        budgetRepository.save(monthlyBudget3);

        // when
        Optional<DailyBudget> latestDaily = budgetRepository.findFirstDailyBudgetByMemberProfileId(testProfile.getId());
        Optional<MonthlyBudget> latestMonthly = budgetRepository.findFirstMonthlyBudgetByMemberProfileId(testProfile.getId());

        // then - 가장 최신 날짜/년월의 예산이 조회되어야 함
        assertThat(latestDaily).isPresent();
        assertThat(latestDaily.get().getDate()).isEqualTo(baseDate.plusDays(10));
        assertThat(latestDaily.get().getLimit()).isEqualTo(BigDecimal.valueOf(30000));

        assertThat(latestMonthly).isPresent();
        assertThat(latestMonthly.get().getYearMonth()).isEqualTo(baseMonth.plusMonths(3));
        assertThat(latestMonthly.get().getLimit()).isEqualTo(BigDecimal.valueOf(600000));
    }

    @DisplayName("날짜 범위 조회 쿼리가 정확한 경계값으로 동작한다")
    @Test
    void findBudgetsByDateRangeBoundaryTest() {
        // given - 다양한 날짜의 일일 예산 생성
        LocalDate baseDate = LocalDate.of(2025, 6, 15);
        
        for (int i = -5; i <= 5; i++) {
            LocalDate date = baseDate.plusDays(i);
            DailyBudget budget = new DailyBudget(profile1, BigDecimal.valueOf(10000 + Math.abs(i) * 1000), date);
            budgetRepository.save(budget);
        }

        // when - 정확한 범위로 조회
        LocalDate startDate = baseDate.minusDays(2); // 6월 13일
        LocalDate endDate = baseDate.plusDays(2);    // 6월 17일
        
        List<DailyBudget> budgetsInRange = budgetRepository.findDailyBudgetsByMemberProfileIdAndDateBetween(
                profile1.getId(), startDate, endDate);

        // then - 경계값 포함하여 5개 조회되어야 함
        assertThat(budgetsInRange).hasSize(5);
        
        for (DailyBudget budget : budgetsInRange) {
            assertThat(budget.getDate()).isBetween(startDate, endDate);
        }

        // 경계값 테스트
        assertThat(budgetsInRange.stream()
                .anyMatch(b -> b.getDate().equals(startDate))).isTrue();
        assertThat(budgetsInRange.stream()
                .anyMatch(b -> b.getDate().equals(endDate))).isTrue();
    }

    @DisplayName("프로필별 예산 격리가 올바르게 동작한다")
    @Test
    void budgetIsolationByProfileTest() {
        // given - 같은 날짜에 두 프로필의 예산 생성
        LocalDate sameDate = LocalDate.now();
        YearMonth sameMonth = YearMonth.now();

        // 프로필1의 예산들
        DailyBudget daily1 = new DailyBudget(profile1, BigDecimal.valueOf(20000), sameDate);
        MonthlyBudget monthly1 = new MonthlyBudget(profile1, BigDecimal.valueOf(500000), sameMonth);
        budgetRepository.save(daily1);
        budgetRepository.save(monthly1);

        // 프로필2의 예산들
        DailyBudget daily2 = new DailyBudget(profile2, BigDecimal.valueOf(30000), sameDate);
        MonthlyBudget monthly2 = new MonthlyBudget(profile2, BigDecimal.valueOf(600000), sameMonth);
        budgetRepository.save(daily2);
        budgetRepository.save(monthly2);

        // when
        Optional<DailyBudget> profile1Daily = budgetRepository.findDailyBudgetByMemberProfileIdAndDate(profile1.getId(), sameDate);
        Optional<DailyBudget> profile2Daily = budgetRepository.findDailyBudgetByMemberProfileIdAndDate(profile2.getId(), sameDate);
        
        Optional<MonthlyBudget> profile1Monthly = budgetRepository.findMonthlyBudgetByMemberProfileIdAndYearMonth(profile1.getId(), sameMonth);
        Optional<MonthlyBudget> profile2Monthly = budgetRepository.findMonthlyBudgetByMemberProfileIdAndYearMonth(profile2.getId(), sameMonth);

        // then - 각 프로필별로 올바른 예산이 조회되어야 함
        assertThat(profile1Daily).isPresent();
        assertThat(profile1Daily.get().getLimit()).isEqualTo(BigDecimal.valueOf(20000));

        assertThat(profile2Daily).isPresent();
        assertThat(profile2Daily.get().getLimit()).isEqualTo(BigDecimal.valueOf(30000));

        assertThat(profile1Monthly).isPresent();
        assertThat(profile1Monthly.get().getLimit()).isEqualTo(BigDecimal.valueOf(500000));

        assertThat(profile2Monthly).isPresent();
        assertThat(profile2Monthly.get().getLimit()).isEqualTo(BigDecimal.valueOf(600000));

        // 서로 다른 객체여야 함
        assertThat(profile1Daily.get().getId()).isNotEqualTo(profile2Daily.get().getId());
        assertThat(profile1Monthly.get().getId()).isNotEqualTo(profile2Monthly.get().getId());
    }

    @DisplayName("대량 데이터에서의 쿼리 성능 및 정확성 테스트")
    @Test
    void largeDataQueryPerformanceTest() {
        // given - 고유한 프로필로 테스트
        Member performanceMember = Member.builder()
                .email("performance@test.com")
                .rawPassword("password123!")
                .build();
        memberRepository.save(performanceMember);

        MemberProfile performanceProfile = MemberProfile.builder()
                .nickName("performanceTestUser")
                .member(performanceMember)
                .build();
        memberProfileRepository.save(performanceProfile);

        // 대량의 일일 예산 데이터 생성 (서로 다른 날짜)
        LocalDate startDate = LocalDate.of(2024, 1, 1); // 2024년 데이터로 변경
        
        for (int i = 0; i < 365; i++) { // 1년치 데이터
            LocalDate date = startDate.plusDays(i);
            DailyBudget budget = new DailyBudget(performanceProfile, BigDecimal.valueOf(20000 + i), date);
            budgetRepository.save(budget);
        }

        // when - 다양한 쿼리 실행
        long startTime = System.currentTimeMillis();
        
        List<DailyBudget> allDailyBudgets = budgetRepository.findDailyBudgetsViaType(performanceProfile.getId());
        Optional<DailyBudget> latestBudget = budgetRepository.findFirstDailyBudgetByMemberProfileId(performanceProfile.getId());
        
        // 1개월 범위 조회
        LocalDate monthStart = LocalDate.of(2024, 6, 1);
        LocalDate monthEnd = LocalDate.of(2024, 6, 30);
        List<DailyBudget> monthlyData = budgetRepository.findDailyBudgetsByMemberProfileIdAndDateBetween(
                performanceProfile.getId(), monthStart, monthEnd);
        
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        // then - 성능 및 정확성 검증
        assertThat(allDailyBudgets).hasSize(365);
        assertThat(latestBudget).isPresent();
        assertThat(latestBudget.get().getDate()).isEqualTo(startDate.plusDays(364)); // 2024년 12월 31일
        
        assertThat(monthlyData).hasSize(30); // 6월은 30일
        assertThat(executionTime).isLessThan(5000); // 5초 이내 실행

        // 데이터 정확성 검증
        for (DailyBudget budget : monthlyData) {
            assertThat(budget.getDate().getMonth().getValue()).isEqualTo(6);
            assertThat(budget.getDate().getYear()).isEqualTo(2024);
        }
    }

    @DisplayName("복합 조건 쿼리 및 정렬 테스트")
    @Test
    void complexQueryAndSortingTest() {
        // given - 고유한 프로필로 테스트
        Member complexMember = Member.builder()
                .email("complex@test.com")
                .rawPassword("password123!")
                .build();
        memberRepository.save(complexMember);

        MemberProfile complexProfile = MemberProfile.builder()
                .nickName("complexTestUser")
                .member(complexMember)
                .build();
        memberProfileRepository.save(complexProfile);

        // 복잡한 시나리오의 데이터 생성
        LocalDate testDate = LocalDate.of(2025, 7, 1);  // 고정된 날짜 사용
        YearMonth testMonth = YearMonth.of(2025, 7);

        // 한 주간의 일일 예산들 (월요일~일요일) - 각기 다른 날짜
        LocalDate monday = testDate.with(java.time.DayOfWeek.MONDAY);
        for (int i = 0; i < 7; i++) {
            LocalDate date = monday.plusDays(i);
            BigDecimal limit = BigDecimal.valueOf(15000 + i * 2000); // 점진적 증가
            DailyBudget budget = new DailyBudget(complexProfile, limit, date);
            budget.addSpent(5000 + i * 1000); // 지출도 점진적 증가
            budgetRepository.save(budget);
        }

        // 여러 월의 월별 예산들 - 각기 다른 년월
        for (int i = 0; i < 6; i++) {
            YearMonth month = testMonth.plusMonths(i); // 미래 월로 변경하여 충돌 방지
            MonthlyBudget budget = new MonthlyBudget(complexProfile, BigDecimal.valueOf(400000 + i * 50000), month);
            budget.addSpent(200000 + i * 30000);
            budgetRepository.save(budget);
        }

        // when
        List<DailyBudget> weeklyBudgets = budgetRepository.findDailyBudgetsByMemberProfileIdAndDateBetween(
                complexProfile.getId(), monday, monday.plusDays(6));
        
        Optional<DailyBudget> latestDaily = budgetRepository.findFirstDailyBudgetByMemberProfileId(complexProfile.getId());
        Optional<MonthlyBudget> latestMonthly = budgetRepository.findFirstMonthlyBudgetByMemberProfileId(complexProfile.getId());

        // then - 정렬 및 데이터 검증
        assertThat(weeklyBudgets).hasSize(7);
        
        // 날짜 순 정렬 확인 (쿼리에서 정렬하지 않으므로 ID 순으로 조회됨)
        for (int i = 0; i < weeklyBudgets.size() - 1; i++) {
            DailyBudget current = weeklyBudgets.get(i);
            DailyBudget next = weeklyBudgets.get(i + 1);
            // 생성 순서 확인 (ID가 증가하는 순서)
            assertThat(current.getId()).isLessThan(next.getId());
        }

        // 최신 예산 검증
        assertThat(latestDaily).isPresent();
        assertThat(latestDaily.get().getDate()).isEqualTo(monday.plusDays(6)); // 일요일

        assertThat(latestMonthly).isPresent();
        assertThat(latestMonthly.get().getYearMonth()).isEqualTo(testMonth.plusMonths(5)); // 가장 미래 월

        // 지출 금액 검증
        for (int i = 0; i < weeklyBudgets.size(); i++) {
            DailyBudget budget = weeklyBudgets.get(i);
            BigDecimal expectedSpent = BigDecimal.valueOf(5000 + i * 1000);
            assertThat(budget.getSpendAmount()).isEqualTo(expectedSpent);
        }
    }

    @DisplayName("null 값 및 edge case 처리 테스트")
    @Test
    void nullValueAndEdgeCaseTest() {
        // given - 존재하지 않는 데이터 조회 시나리오

        // when - 존재하지 않는 프로필 ID로 조회
        Long nonExistentProfileId = 999999L;
        List<DailyBudget> emptyDailyList = budgetRepository.findDailyBudgetsViaType(nonExistentProfileId);
        List<MonthlyBudget> emptyMonthlyList = budgetRepository.findMonthlyBudgetsViaType(nonExistentProfileId);
        
        Optional<DailyBudget> emptyDailyOpt = budgetRepository.findDailyBudgetByMemberProfileIdAndDate(
                nonExistentProfileId, LocalDate.now());
        Optional<MonthlyBudget> emptyMonthlyOpt = budgetRepository.findMonthlyBudgetByMemberProfileIdAndYearMonth(
                nonExistentProfileId, YearMonth.now());

        // then - 빈 결과 반환
        assertThat(emptyDailyList).isEmpty();
        assertThat(emptyMonthlyList).isEmpty();
        assertThat(emptyDailyOpt).isEmpty();
        assertThat(emptyMonthlyOpt).isEmpty();

        // when - 존재하지 않는 날짜로 조회
        LocalDate futureDate = LocalDate.of(2030, 12, 31);
        YearMonth futureMonth = YearMonth.of(2030, 12);
        
        Optional<DailyBudget> futureDailyOpt = budgetRepository.findDailyBudgetByMemberProfileIdAndDate(
                profile1.getId(), futureDate);
        Optional<MonthlyBudget> futureMonthlyOpt = budgetRepository.findMonthlyBudgetByMemberProfileIdAndYearMonth(
                profile1.getId(), futureMonth);

        // then - 빈 결과 반환
        assertThat(futureDailyOpt).isEmpty();
        assertThat(futureMonthlyOpt).isEmpty();
    }
} 