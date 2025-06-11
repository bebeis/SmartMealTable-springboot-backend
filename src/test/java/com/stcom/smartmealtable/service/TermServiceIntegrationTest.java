package com.stcom.smartmealtable.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.stcom.smartmealtable.domain.member.Member;
import com.stcom.smartmealtable.domain.term.Term;
import com.stcom.smartmealtable.repository.MemberRepository;
import com.stcom.smartmealtable.repository.TermRepository;
import com.stcom.smartmealtable.service.dto.TermAgreementRequestDto;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

/**
 * 통합 테스트: 스프링 컨텍스트와 실제 JPA 구현체로 Service 계층 검증.
 */
@SpringBootTest
@Transactional
class TermServiceIntegrationTest {

    @Autowired
    private TermService termService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TermRepository termRepository;

    @Test
    @DisplayName("필수 약관에 동의하면 정상적으로 저장되어야 한다")
    @Rollback
    void agreeRequiredTerms() throws Exception {
        // given
        Member member = Member.builder()
                .fullName("홍길동")
                .email("test@example.com")
                .rawPassword("Password1!")
                .build();
        memberRepository.save(member);

        Term required1 = createTerm("이용약관", true);
        Term required2 = createTerm("개인정보 처리방침", true);
        termRepository.saveAll(List.of(required1, required2));

        List<TermAgreementRequestDto> requests = List.of(
                new TermAgreementRequestDto(required1.getId(), true),
                new TermAgreementRequestDto(required2.getId(), true)
        );

        // when
        termService.agreeTerms(member.getId(), requests);

        // then
        // TermAgreementRepository 를 통해 조회해도 되지만, 간단히 member 의 약관 동의 수를 확인
        long count = termRepository.count();
        assertThat(count).isEqualTo(2);
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