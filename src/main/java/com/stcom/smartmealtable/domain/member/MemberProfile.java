package com.stcom.smartmealtable.domain.member;

import com.stcom.smartmealtable.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

@Entity
public class MemberProfile extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "member_profile_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private MemberAuth memberAuth;

    private String username;
}
