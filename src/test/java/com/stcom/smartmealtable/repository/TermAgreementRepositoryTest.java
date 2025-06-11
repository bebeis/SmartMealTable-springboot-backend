package com.stcom.smartmealtable.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.stcom.smartmealtable.domain.member.Member;
import com.stcom.smartmealtable.domain.term.Term;
import com.stcom.smartmealtable.domain.term.TermAgreement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest
@ActiveProfiles("test")
class TermAgreementRepositoryTest {

    @Autowired
    private TermAgreementRepository repository;

    @Autowired
    private TermRepository termRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("약관 동의 엔티티를 저장하고 조회할 수 있다")
    void saveAndFind() {
        // given
        Member member = new Member("agree@test.com");
        memberRepository.save(member);

        Term term = new Term();
        ReflectionTestUtils.setField(term, "title", "테스트 약관");
        termRepository.save(term);

        TermAgreement agreement = TermAgreement.builder()
                .member(member)
                .term(term)
                .isAgreed(true)
                .build();
        repository.save(agreement);
        em.flush();
        em.clear();

        // when
        var found = repository.findById(agreement.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getTerm().getTitle()).isEqualTo("테스트 약관");
        assertThat(found.get().getMember().getEmail()).isEqualTo("agree@test.com");
    }
} 