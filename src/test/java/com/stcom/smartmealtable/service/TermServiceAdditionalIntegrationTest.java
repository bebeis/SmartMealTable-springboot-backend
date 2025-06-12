package com.stcom.smartmealtable.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.stcom.smartmealtable.domain.member.Member;
import com.stcom.smartmealtable.domain.term.Term;
import com.stcom.smartmealtable.domain.term.TermAgreement;
import com.stcom.smartmealtable.repository.MemberRepository;
import com.stcom.smartmealtable.repository.TermAgreementRepository;
import com.stcom.smartmealtable.repository.TermRepository;
import com.stcom.smartmealtable.service.dto.TermAgreementRequestDto;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
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
class TermServiceAdditionalIntegrationTest {

    @Autowired
    private TermService termService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TermRepository termRepository;

    @Autowired
    private TermAgreementRepository termAgreementRepository;

    private Member member;
    private Term requiredTerm;
    private Term optionalTerm;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .email("term_test@example.com")
                .rawPassword("password123!")
                .build();
        memberRepository.save(member);

        requiredTerm = createTerm("필수 약관", true);
        optionalTerm = createTerm("선택 약관", false);
        termRepository.saveAll(Arrays.asList(requiredTerm, optionalTerm));
    }

    @Test
    @DisplayName("필수 약관에 동의하지 않으면 예외가 발생한다")
    void agreeTermsWithoutRequiredTerms() {
        // given
        List<TermAgreementRequestDto> requests = Collections.singletonList(
                new TermAgreementRequestDto(requiredTerm.getId(), false)
        );

        // when & then
        assertThatThrownBy(() -> termService.agreeTerms(member.getId(), requests))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("필수 약관에 동의해야 합니다");
    }

    @Test
    @DisplayName("존재하지 않는 회원 ID로 약관 동의를 시도하면 예외가 발생한다")
    void agreeTermsWithInvalidMemberId() {
        // given
        Long invalidMemberId = 9999L;
        List<TermAgreementRequestDto> requests = Collections.singletonList(
                new TermAgreementRequestDto(requiredTerm.getId(), true)
        );

        // when & then
        assertThatThrownBy(() -> termService.agreeTerms(invalidMemberId, requests))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 회원입니다");
    }

    @Test
    @DisplayName("존재하지 않는 약관 ID로 약관 동의를 시도하면 예외가 발생한다")
    void agreeTermsWithInvalidTermId() {
        // given
        Long invalidTermId = 9999L;
        
        // 모든 필수 약관에 동의하는 리스트를 만든 다음 존재하지 않는 약관 ID를 추가
        List<TermAgreementRequestDto> requests = new java.util.ArrayList<>();
        // 필수 약관에 먼저 동의 (필수 약관 검증을 통과하기 위해)
        requests.add(new TermAgreementRequestDto(requiredTerm.getId(), true));
        // 존재하지 않는 약관 ID 추가
        requests.add(new TermAgreementRequestDto(invalidTermId, true));

        // when & then
        assertThatThrownBy(() -> termService.agreeTerms(member.getId(), requests))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 약관입니다");
    }

    @Test
    @DisplayName("필수 약관을 포함하지 않고 약관 동의를 시도하면 예외가 발생한다")
    void agreeTermsWithMissingRequiredTerm() {
        // given
        // 선택 약관만 동의
        List<TermAgreementRequestDto> requests = Collections.singletonList(
                new TermAgreementRequestDto(optionalTerm.getId(), true)
        );

        // when & then
        assertThatThrownBy(() -> termService.agreeTerms(member.getId(), requests))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("필수 약관에 동의해야 합니다");
    }

    @Test
    @DisplayName("모든 약관에 동의하면 정상적으로 저장된다")
    void agreeAllTerms() {
        // given
        List<TermAgreementRequestDto> requests = Arrays.asList(
                new TermAgreementRequestDto(requiredTerm.getId(), true),
                new TermAgreementRequestDto(optionalTerm.getId(), true)
        );

        // when
        termService.agreeTerms(member.getId(), requests);

        // then
        List<TermAgreement> agreements = termAgreementRepository.findAll().stream()
                .filter(a -> a.getMember().getId().equals(member.getId()))
                .collect(Collectors.toList());
        assertThat(agreements).hasSize(2);
        assertThat(agreements.stream().allMatch(TermAgreement::getIsAgreed)).isTrue();
    }

    @Test
    @DisplayName("필수 약관에만 동의하고 선택 약관에는 동의하지 않아도 정상적으로 저장된다")
    void agreeRequiredTermsOnly() {
        // given
        List<TermAgreementRequestDto> requests = Arrays.asList(
                new TermAgreementRequestDto(requiredTerm.getId(), true),
                new TermAgreementRequestDto(optionalTerm.getId(), false)
        );

        // when
        termService.agreeTerms(member.getId(), requests);

        // then
        List<TermAgreement> agreements = termAgreementRepository.findAll().stream()
                .filter(a -> a.getMember().getId().equals(member.getId()))
                .collect(Collectors.toList());
        assertThat(agreements).hasSize(2);
        
        TermAgreement requiredAgreement = agreements.stream()
                .filter(a -> a.getTerm().getId().equals(requiredTerm.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(requiredAgreement.getIsAgreed()).isTrue();
        
        TermAgreement optionalAgreement = agreements.stream()
                .filter(a -> a.getTerm().getId().equals(optionalTerm.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(optionalAgreement.getIsAgreed()).isFalse();
    }

    @Test
    @DisplayName("모든 약관 목록을 조회할 수 있다")
    void findAllTerms() {
        // given
        // 추가 약관 생성
        Term additionalTerm = createTerm("추가 약관", true);
        termRepository.save(additionalTerm);

        // when
        List<Term> terms = termService.findAll();

        // then
        assertThat(terms).hasSize(3);
        assertThat(terms.stream().map(Term::getTitle).toList())
                .contains("필수 약관", "선택 약관", "추가 약관");
    }

    private Term createTerm(String title, boolean required) {
        Term term = new Term();
        try {
            java.lang.reflect.Field titleField = Term.class.getDeclaredField("title");
            titleField.setAccessible(true);
            titleField.set(term, title);

            java.lang.reflect.Field isRequiredField = Term.class.getDeclaredField("isRequired");
            isRequiredField.setAccessible(true);
            isRequiredField.set(term, required);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return term;
    }
} 