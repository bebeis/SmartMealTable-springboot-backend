package com.stcom.smartmealtable.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.stcom.smartmealtable.domain.Budget.DailyBudget;
import com.stcom.smartmealtable.domain.Budget.Expenditure;
import com.stcom.smartmealtable.domain.Budget.MonthlyBudget;
import com.stcom.smartmealtable.domain.member.Member;
import com.stcom.smartmealtable.domain.member.MemberProfile;
import com.stcom.smartmealtable.repository.BudgetRepository;
import com.stcom.smartmealtable.repository.ExpenditureRepository;
import com.stcom.smartmealtable.repository.MemberProfileRepository;
import com.stcom.smartmealtable.repository.MemberRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import(ExpenditureService.class)
class ExpenditureServiceIntegrationTest {

    @Autowired
    private ExpenditureService expenditureService;
    @Autowired
    private ExpenditureRepository expenditureRepository;
    @Autowired
    private BudgetRepository budgetRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private MemberProfileRepository memberProfileRepository;

    private Member member;
    private MemberProfile profile;
    private DailyBudget dailyBudget;
    private MonthlyBudget monthlyBudget;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .email("exptest@example.com")
                .rawPassword("P@ssw0rd!")
                .build();
        memberRepository.save(member);

        profile = MemberProfile.builder()
                .member(member)
                .nickName("tester")
                .build();
        memberProfileRepository.save(profile);

        LocalDate today = LocalDate.now();
        YearMonth thisMonth = YearMonth.from(today);

        dailyBudget = new DailyBudget(profile, BigDecimal.valueOf(50_000), today);
        monthlyBudget = new MonthlyBudget(profile, BigDecimal.valueOf(1_000_000), thisMonth);
        budgetRepository.save(dailyBudget);
        budgetRepository.save(monthlyBudget);
    }

    @DisplayName("지출 등록 시 Expenditure 저장 및 해당 예산 사용금액이 증가한다")
    @Test
    void registerExpenditure() {
        // given
        LocalDateTime spentDate = LocalDateTime.now();
        Long amount = 12000L;
        String tradeName = "Lunch";

        // when
        expenditureService.registerExpenditure(profile.getId(), spentDate, amount, tradeName);

        // then
        Expenditure saved = expenditureRepository.findAll().getFirst();
        assertThat(saved).isNotNull();
        assertThat(saved.getTradeName()).isEqualTo(tradeName);
        assertThat(saved.getAmount()).isEqualTo(amount);
        assertThat(saved.getDailyBudget().getId()).isEqualTo(dailyBudget.getId());
        assertThat(saved.getMonthlyBudget().getId()).isEqualTo(monthlyBudget.getId());

        DailyBudget reloadedDaily = budgetRepository.findDailyBudgetByMemberProfileIdAndDate(profile.getId(),
                dailyBudget.getDate()).orElseThrow();
        MonthlyBudget reloadedMonthly = budgetRepository.findMonthlyBudgetByMemberProfileIdAndYearMonth(profile.getId(),
                monthlyBudget.getYearMonth()).orElseThrow();

        assertThat(reloadedDaily.getSpendAmount()).isEqualTo(BigDecimal.valueOf(amount));
        assertThat(reloadedMonthly.getSpendAmount()).isEqualTo(BigDecimal.valueOf(amount));
    }

    @DisplayName("지출 내역을 페이징으로 조회할 수 있다")
    @Test
    void getExpenditures() {
        // given
        LocalDateTime spentDate1 = LocalDateTime.now().withHour(9).withMinute(0);
        LocalDateTime spentDate2 = LocalDateTime.now().withHour(12).withMinute(0);
        LocalDateTime spentDate3 = LocalDateTime.now().withHour(18).withMinute(0);
        
        expenditureService.registerExpenditure(profile.getId(), spentDate1, 10000L, "Breakfast");
        expenditureService.registerExpenditure(profile.getId(), spentDate2, 15000L, "Lunch");
        expenditureService.registerExpenditure(profile.getId(), spentDate3, 20000L, "Dinner");

        // when
        var result = expenditureService.getExpenditures(profile.getId(), 0, 2);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getTradeName()).isEqualTo("Dinner"); // 최신순
        assertThat(result.getContent().get(1).getTradeName()).isEqualTo("Lunch");
        assertThat(result.hasNext()).isTrue();
    }

    @DisplayName("지출 내역을 수정할 수 있다")
    @Test
    void editExpenditure() {
        // given
        LocalDateTime originalSpentDate = LocalDateTime.now();
        Long originalAmount = 12000L;
        String originalTradeName = "Lunch";
        
        expenditureService.registerExpenditure(profile.getId(), originalSpentDate, originalAmount, originalTradeName);
        Expenditure savedExpenditure = expenditureRepository.findAll().getFirst();
        
        LocalDateTime newSpentDate = LocalDateTime.now().plusHours(1);
        Long newAmount = 15000L;
        String newTradeName = "Dinner";

        // when
        expenditureService.editExpenditure(profile.getId(), savedExpenditure.getId(), newSpentDate, newAmount, newTradeName);

        // then
        Expenditure updatedExpenditure = expenditureRepository.findById(savedExpenditure.getId()).orElseThrow();
        assertThat(updatedExpenditure.getSpentDate()).isEqualTo(newSpentDate);
        assertThat(updatedExpenditure.getAmount()).isEqualTo(newAmount);
        assertThat(updatedExpenditure.getTradeName()).isEqualTo(newTradeName);
    }

    @DisplayName("지출 내역을 삭제하면 예산에서 해당 금액이 차감된다")
    @Test
    void deleteExpenditure() {
        // given
        LocalDateTime spentDate = LocalDateTime.now();
        Long amount = 12000L;
        String tradeName = "Lunch";
        
        expenditureService.registerExpenditure(profile.getId(), spentDate, amount, tradeName);
        Expenditure savedExpenditure = expenditureRepository.findAll().getFirst();
        
        // 삭제 전 예산 확인
        DailyBudget beforeDeleteDaily = budgetRepository.findDailyBudgetByMemberProfileIdAndDate(profile.getId(),
                dailyBudget.getDate()).orElseThrow();
        MonthlyBudget beforeDeleteMonthly = budgetRepository.findMonthlyBudgetByMemberProfileIdAndYearMonth(profile.getId(),
                monthlyBudget.getYearMonth()).orElseThrow();
        
        assertThat(beforeDeleteDaily.getSpendAmount()).isEqualTo(BigDecimal.valueOf(amount));
        assertThat(beforeDeleteMonthly.getSpendAmount()).isEqualTo(BigDecimal.valueOf(amount));

        // when
        expenditureService.deleteExpenditure(profile.getId(), savedExpenditure.getId());

        // then
        assertThat(expenditureRepository.findById(savedExpenditure.getId())).isEmpty();
        
        DailyBudget afterDeleteDaily = budgetRepository.findDailyBudgetByMemberProfileIdAndDate(profile.getId(),
                dailyBudget.getDate()).orElseThrow();
        MonthlyBudget afterDeleteMonthly = budgetRepository.findMonthlyBudgetByMemberProfileIdAndYearMonth(profile.getId(),
                monthlyBudget.getYearMonth()).orElseThrow();
        
        assertThat(afterDeleteDaily.getSpendAmount()).isEqualTo(BigDecimal.ZERO);
        assertThat(afterDeleteMonthly.getSpendAmount()).isEqualTo(BigDecimal.ZERO);
    }

    @DisplayName("존재하지 않는 지출 내역 수정 시 예외가 발생한다")
    @Test
    void editExpenditure_NotFound() {
        // given
        Long nonExistentExpenditureId = 999L;
        LocalDateTime spentDate = LocalDateTime.now();
        Long amount = 12000L;
        String tradeName = "Lunch";

        // when & then
        assertThatThrownBy(() -> expenditureService.editExpenditure(profile.getId(), nonExistentExpenditureId, spentDate, amount, tradeName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("지출 내역이 존재하지 않습니다.");
    }

    @DisplayName("존재하지 않는 지출 내역 삭제 시 예외가 발생한다")
    @Test
    void deleteExpenditure_NotFound() {
        // given
        Long nonExistentExpenditureId = 999L;

        // when & then
        assertThatThrownBy(() -> expenditureService.deleteExpenditure(profile.getId(), nonExistentExpenditureId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("지출 내역이 존재하지 않습니다.");
    }

} 