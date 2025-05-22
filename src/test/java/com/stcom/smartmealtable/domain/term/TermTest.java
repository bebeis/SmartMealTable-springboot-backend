package com.stcom.smartmealtable.domain.term;

import static org.assertj.core.api.Assertions.assertThat;

import com.stcom.smartmealtable.domain.member.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class TermTest {

    @Test
    @DisplayName("Term 객체의 필드값이 올바르게 설정된다")
    void termFieldsAreSetCorrectly() {
        // given
        Term term = new Term();
        
        // when
        ReflectionTestUtils.setField(term, "id", 1L);
        ReflectionTestUtils.setField(term, "version", 1);
        ReflectionTestUtils.setField(term, "title", "이용약관");
        ReflectionTestUtils.setField(term, "content", "이용약관 내용입니다.");
        ReflectionTestUtils.setField(term, "isRequired", true);
        
        // then
        assertThat(term.getId()).isEqualTo(1L);
        assertThat(term.getVersion()).isEqualTo(1);
        assertThat(term.getTitle()).isEqualTo("이용약관");
        assertThat(term.getContent()).isEqualTo("이용약관 내용입니다.");
        assertThat(term.getIsRequired()).isTrue();
    }
    
    @Test
    @DisplayName("필수 약관과 선택 약관을 구분할 수 있다")
    void canDistinguishBetweenRequiredAndOptionalTerms() {
        // given
        Term requiredTerm = new Term();
        Term optionalTerm = new Term();
        
        // when
        ReflectionTestUtils.setField(requiredTerm, "isRequired", true);
        ReflectionTestUtils.setField(optionalTerm, "isRequired", false);
        
        // then
        assertThat(requiredTerm.getIsRequired()).isTrue();
        assertThat(optionalTerm.getIsRequired()).isFalse();
    }
}

class TermAgreementTest {

    @Test
    @DisplayName("TermAgreement 빌더를 통해 객체가 올바르게 생성된다")
    void termAgreementIsCreatedCorrectlyUsingBuilder() {
        // given
        Member member = new Member();
        Term term = new Term();
        
        ReflectionTestUtils.setField(term, "id", 1L);
        ReflectionTestUtils.setField(term, "title", "이용약관");
        
        // when
        TermAgreement termAgreement = TermAgreement.builder()
                .member(member)
                .term(term)
                .isAgreed(true)
                .build();
        
        // then
        assertThat(termAgreement.getMember()).isEqualTo(member);
        assertThat(termAgreement.getTerm()).isEqualTo(term);
        assertThat(termAgreement.getTerm().getId()).isEqualTo(1L);
        assertThat(termAgreement.getIsAgreed()).isTrue();
    }
    
    @Test
    @DisplayName("필수 약관 동의 여부를 확인할 수 있다")
    void canCheckIfRequiredTermIsAgreed() {
        // given
        Member member = new Member();
        Term requiredTerm = new Term();
        
        ReflectionTestUtils.setField(requiredTerm, "isRequired", true);
        
        // when
        TermAgreement agreedTermAgreement = TermAgreement.builder()
                .member(member)
                .term(requiredTerm)
                .isAgreed(true)
                .build();
                
        TermAgreement disagreedTermAgreement = TermAgreement.builder()
                .member(member)
                .term(requiredTerm)
                .isAgreed(false)
                .build();
        
        // then
        assertThat(agreedTermAgreement.getTerm().getIsRequired()).isTrue();
        assertThat(agreedTermAgreement.getIsAgreed()).isTrue();
        
        assertThat(disagreedTermAgreement.getTerm().getIsRequired()).isTrue();
        assertThat(disagreedTermAgreement.getIsAgreed()).isFalse();
    }
} 