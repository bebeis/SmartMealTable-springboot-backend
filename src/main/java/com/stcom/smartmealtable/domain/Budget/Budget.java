package com.stcom.smartmealtable.domain.Budget;

import com.stcom.smartmealtable.domain.common.BaseTimeEntity;
import com.stcom.smartmealtable.domain.member.Member;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn
@NoArgsConstructor
public abstract class Budget extends BaseTimeEntity {

    @Id
    @GeneratedValue
    @Column(name = "budget_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private BigDecimal spendAmount = BigDecimal.ZERO;

    @Column(name = "budget_limit")
    private BigDecimal limit;

    protected Budget(Member member, BigDecimal limit) {
        this.member = member;
        this.limit = limit;
    }

    public void addSpent(BigDecimal amount) {
        this.spendAmount = spendAmount.add(amount);
    }

    public void addSpent(int amount) {
        this.spendAmount = spendAmount.add(BigDecimal.valueOf(amount));
    }

    public void addSpent(double amount) {
        this.spendAmount = spendAmount.add(BigDecimal.valueOf(amount));
    }

    public void resetSpent() {
        this.spendAmount = BigDecimal.ZERO;
    }

    public BigDecimal getAvailableAmount() {
        return limit.subtract(spendAmount);
    }

    public boolean isOverLimit() {
        return spendAmount.compareTo(limit) > 0;
    }
}
