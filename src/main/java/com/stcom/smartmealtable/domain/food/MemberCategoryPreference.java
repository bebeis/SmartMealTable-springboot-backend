package com.stcom.smartmealtable.domain.food;

import com.stcom.smartmealtable.domain.member.MemberProfile;
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
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table
@NoArgsConstructor
@Getter
public class MemberCategoryPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_category_preference_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_profile_id")
    private MemberProfile memberProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_category_id")
    private FoodCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PreferenceType type;  // LIKE or DISLIKE

    @Column(nullable = false)
    private Integer priority;

    private Double weight;

    @Builder
    public MemberCategoryPreference(MemberProfile memberProfile,
                                    FoodCategory category,
                                    PreferenceType type,
                                    Integer priority) {
        this.memberProfile = memberProfile;
        this.category = category;
        this.type = type;
        this.priority = priority;
    }
}
