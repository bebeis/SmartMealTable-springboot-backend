package com.stcom.smartmealtable.domain.Budget;

import com.stcom.smartmealtable.domain.common.BaseTimeEntity;
import com.stcom.smartmealtable.domain.member.MemberProfile;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "budget_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_profile_id")
    private MemberProfile memberProfile;

    private BigDecimal spendAmount = BigDecimal.ZERO;

    @Column(name = "budget_limit")
    private BigDecimal limit;

    protected Budget(MemberProfile memberProfile, BigDecimal limit) {
        this.memberProfile = memberProfile;
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

    public void changeLimit(BigDecimal limit) {
        if (limit == null || limit.signum() < 0) {
            throw new IllegalArgumentException("예산 한도는 0 이상이어야 합니다.");
        }
        this.limit = limit;
    }
}
