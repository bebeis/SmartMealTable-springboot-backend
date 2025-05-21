package com.stcom.smartmealtable.domain.term;

import com.stcom.smartmealtable.domain.common.BaseTimeEntity;
import com.stcom.smartmealtable.domain.member.Member;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class TermAgreement extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "term_id")
    private Term term;

    private Boolean isAgreed;

    @Builder
    public TermAgreement(Member member, Term term, Boolean isAgreed) {
        this.member = member;
        this.term = term;
        this.isAgreed = isAgreed;
    }
}
