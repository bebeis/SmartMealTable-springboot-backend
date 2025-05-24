package com.stcom.smartmealtable.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stcom.smartmealtable.domain.member.Member;
import com.stcom.smartmealtable.domain.term.Term;
import com.stcom.smartmealtable.domain.term.TermAgreement;
import com.stcom.smartmealtable.repository.MemberRepository;
import com.stcom.smartmealtable.repository.TermAgreementRepository;
import com.stcom.smartmealtable.repository.TermRepository;
import com.stcom.smartmealtable.service.dto.TermAgreementRequestDto;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TermServiceTest {

    @InjectMocks
    private TermService termService;

    @Mock
    private TermRepository termRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private TermAgreementRepository termAgreementRepository;

    @Test
    @DisplayName("모든 약관을 조회할 수 있다")
    void findAll() {
        // given
        Term term1 = createTerm(1L, "이용약관", true);
        Term term2 = createTerm(2L, "개인정보 처리방침", true);
        Term term3 = createTerm(3L, "마케팅 정보 수신 동의", false);
        
        when(termRepository.findAll()).thenReturn(Arrays.asList(term1, term2, term3));

        // when
        List<Term> terms = termService.findAll();

        // then
        assertThat(terms).hasSize(3);
        assertThat(terms.get(0).getTitle()).isEqualTo("이용약관");
        assertThat(terms.get(1).getTitle()).isEqualTo("개인정보 처리방침");
        assertThat(terms.get(2).getTitle()).isEqualTo("마케팅 정보 수신 동의");
    }

    @Test
    @DisplayName("회원이 약관에 동의할 수 있다")
    void agreeTerms() {
        // given
        Long memberId = 1L;
        Member member = createMember(memberId);
        
        Term term1 = createTerm(1L, "이용약관", true);
        Term term2 = createTerm(2L, "개인정보 처리방침", true);
        Term term3 = createTerm(3L, "마케팅 정보 수신 동의", false);
        
        TermAgreementRequestDto dto1 = new TermAgreementRequestDto(1L, true);
        TermAgreementRequestDto dto2 = new TermAgreementRequestDto(2L, true);
        TermAgreementRequestDto dto3 = new TermAgreementRequestDto(3L, false);
        
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(termRepository.findAll()).thenReturn(Arrays.asList(term1, term2, term3));
        when(termRepository.findById(1L)).thenReturn(Optional.of(term1));
        when(termRepository.findById(2L)).thenReturn(Optional.of(term2));
        when(termRepository.findById(3L)).thenReturn(Optional.of(term3));

        // when
        termService.agreeTerms(memberId, Arrays.asList(dto1, dto2, dto3));

        // then
        verify(termAgreementRepository, times(3)).save(any(TermAgreement.class));
    }

    @Test
    @DisplayName("필수 약관에 동의하지 않으면 예외가 발생한다")
    void agreeTerms_RequiredTermNotAgreed() {
        // given
        Long memberId = 1L;
        Member member = createMember(memberId);
        
        Term term1 = createTerm(1L, "이용약관", true);
        Term term2 = createTerm(2L, "개인정보 처리방침", true);
        
        TermAgreementRequestDto dto1 = new TermAgreementRequestDto(1L, true);
        TermAgreementRequestDto dto2 = new TermAgreementRequestDto(2L, false); // 필수 약관이지만 동의하지 않음
        
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(termRepository.findAll()).thenReturn(Arrays.asList(term1, term2));

        // when & then
        assertThatThrownBy(() -> termService.agreeTerms(memberId, Arrays.asList(dto1, dto2)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("필수 약관에 동의해야 합니다");
    }

    @Test
    @DisplayName("존재하지 않는 회원 ID로 약관 동의를 시도하면 예외가 발생한다")
    void agreeTerms_MemberNotFound() {
        // given
        Long nonExistentMemberId = 999L;
        TermAgreementRequestDto dto = new TermAgreementRequestDto(1L, true);
        
        when(memberRepository.findById(nonExistentMemberId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> termService.agreeTerms(nonExistentMemberId, List.of(dto)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("존재하지 않는 회원입니다");
    }

    @Test
    @DisplayName("존재하지 않는 약관 ID로 약관 동의를 시도하면 예외가 발생한다")
    void agreeTerms_TermNotFound() {
        // given
        Long memberId = 1L;
        Long nonExistentTermId = 999L;
        Member member = createMember(memberId);
        TermAgreementRequestDto dto = new TermAgreementRequestDto(nonExistentTermId, true);
        
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(termRepository.findAll()).thenReturn(List.of());
        when(termRepository.findById(nonExistentTermId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> termService.agreeTerms(memberId, List.of(dto)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("존재하지 않는 약관입니다");
    }
    
    // 테스트용 회원 생성 헬퍼 메소드
    private Member createMember(Long id) {
        Member member = new Member();
        return member;
    }
    
    // 테스트용 약관 생성 헬퍼 메소드
    private Term createTerm(Long id, String title, Boolean isRequired) {
        Term term = new Term();
        // 리플렉션을 통해 private 필드에 값 설정
        try {
            java.lang.reflect.Field idField = Term.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(term, id);
            
            java.lang.reflect.Field titleField = Term.class.getDeclaredField("title");
            titleField.setAccessible(true);
            titleField.set(term, title);
            
            java.lang.reflect.Field isRequiredField = Term.class.getDeclaredField("isRequired");
            isRequiredField.setAccessible(true);
            isRequiredField.set(term, isRequired);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return term;
    }
} 