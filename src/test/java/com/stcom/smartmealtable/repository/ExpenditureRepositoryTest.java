package com.stcom.smartmealtable.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.stcom.smartmealtable.domain.Budget.DailyBudget;
import com.stcom.smartmealtable.domain.Budget.Expenditure;
import com.stcom.smartmealtable.domain.Budget.MonthlyBudget;
import com.stcom.smartmealtable.domain.member.Member;
import com.stcom.smartmealtable.domain.member.MemberProfile;
import com.stcom.smartmealtable.domain.member.MemberType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import com.stcom.smartmealtable.repository.MemberRepository;
import com.stcom.smartmealtable.repository.MemberProfileRepository;

@DataJpaTest
@ActiveProfiles("test")
class ExpenditureRepositoryTest {

    @Autowired
    private ExpenditureRepository expenditureRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberProfileRepository memberProfileRepository;

    @DisplayName("Slice 기반 무한스크롤 조회가 최신순으로 올바르게 동작한다")
    @Test
    void slicePaginationByProfile() {
        // given
        Member member = Member.builder()
                .email("slice@test.com")
                .rawPassword("@Password1")
                .build();
        memberRepository.save(member);

        MemberProfile profile = MemberProfile.builder()
                .member(member)
                .nickName("tester")
                .type(MemberType.OTHER)
                .build();
        memberProfileRepository.save(profile);

        LocalDate today = LocalDate.now();
        YearMonth thisMonth = YearMonth.now();

        DailyBudget dailyBudget = new DailyBudget(profile, BigDecimal.valueOf(10_000), today);
        MonthlyBudget monthlyBudget = new MonthlyBudget(profile, BigDecimal.valueOf(300_000), thisMonth);
        budgetRepository.save(dailyBudget);
        budgetRepository.save(monthlyBudget);

        // 15개의 지출내역 생성 (1분 간격으로 시간 차이)
        IntStream.range(0, 15).forEach(i -> {
            LocalDateTime spentDate = LocalDateTime.now().minusMinutes(i);
            Expenditure expenditure = Expenditure.builder()
                    .spentDate(spentDate)
                    .amount(1000L + i)
                    .tradeName("coffee" + i)
                    .dailyBudget(dailyBudget)
                    .monthlyBudget(monthlyBudget)
                    .build();
            expenditureRepository.save(expenditure);
        });

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "spentDate"));

        // when
        Slice<Expenditure> slice = expenditureRepository
                .findByDailyBudget_MemberProfile_IdOrderBySpentDateDesc(profile.getId(), pageable);

        // then
        assertThat(slice).isNotNull();
        assertThat(slice.getContent()).hasSize(10);
        assertThat(slice.hasNext()).isTrue();

        // spentDate 가 내림차순인지 확인
        boolean sortedDesc = slice.getContent().stream()
                .sorted(Comparator.comparing(Expenditure::getSpentDate).reversed())
                .toList()
                .equals(slice.getContent());
        assertThat(sortedDesc).isTrue();
    }
}