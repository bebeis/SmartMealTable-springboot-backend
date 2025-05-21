package com.stcom.smartmealtable.service;

import com.stcom.smartmealtable.domain.member.Member;
import com.stcom.smartmealtable.domain.term.Term;
import com.stcom.smartmealtable.domain.term.TermAgreement;
import com.stcom.smartmealtable.repository.MemberRepository;
import com.stcom.smartmealtable.repository.TermAgreementRepository;
import com.stcom.smartmealtable.repository.TermRepository;
import com.stcom.smartmealtable.service.dto.TermAgreementRequestDto;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TermService {

    private final TermRepository termRepository;
    private final MemberRepository memberRepository;
    private final TermAgreementRepository termAgreementRepository;

    public List<Term> findAll() {
        return termRepository.findAll();
    }


    public void agreeTerms(Long memberId, List<TermAgreementRequestDto> termAgreements) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다"));

        List<Term> requiredTerms = termRepository.findAll().stream()
                .filter(Term::getIsRequired)
                .toList();

        Map<Long, Boolean> agreementMap = termAgreements.stream()
                .collect(Collectors.toMap(TermAgreementRequestDto::getTermId,
                        TermAgreementRequestDto::getIsAgreed));

        for (Term term : requiredTerms) {
            Boolean agreed = agreementMap.get(term.getId());
            if (agreed == null || !agreed) {
                throw new IllegalArgumentException("필수 약관에 동의해야 합니다: " + term.getTitle());
            }
        }

        // 약관 동의 저장
        for (TermAgreementRequestDto dto : termAgreements) {
            Term term = termRepository.findById(dto.getTermId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 약관입니다: " + dto.getTermId()));
            TermAgreement agreement = TermAgreement.builder()
                    .member(member)
                    .term(term)
                    .isAgreed(dto.getIsAgreed())
                    .build();
            termAgreementRepository.save(agreement);
        }
    }
}
