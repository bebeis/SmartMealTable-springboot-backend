package com.stcom.smartmealtable.domain.food;

import com.stcom.smartmealtable.common.BaseTimeEntity;
import com.stcom.smartmealtable.domain.member.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
public class FoodPreference extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "food_preference_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private FoodCategory category;

    private boolean isPreferred;

    private Double weight;

    @Builder
    public FoodPreference(Member member, FoodCategory category, boolean isPreferred, Double weight) {
        this.member = member;
        this.category = category;
        this.isPreferred = isPreferred;
        this.weight = weight;
    }


    public void updatePreference(boolean isPreferred, Double weight) {
        this.isPreferred = isPreferred;
        this.weight = weight;
    }

}
