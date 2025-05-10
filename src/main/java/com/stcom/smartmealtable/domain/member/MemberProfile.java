package com.stcom.smartmealtable.domain.member;

import com.stcom.smartmealtable.common.BaseTimeEntity;
import com.stcom.smartmealtable.domain.common.Address;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.util.ArrayList;
import java.util.List;

@Entity
public class MemberProfile extends BaseTimeEntity {

    @Id
    @GeneratedValue
    @Column(name = "member_profile_id")
    private Long id;

    @OneToOne(mappedBy = "memberProfile")
    private MemberAuth memberAuth;

    private String fullName;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "member_id")
    private List<Address> addressHistory = new ArrayList<>();

    protected void linkMemberAuth(MemberAuth memberAuth) {
        this.memberAuth = memberAuth;
    }

    public void changeFullName(String newName) {
        if (newName.isBlank()) {
            throw new IllegalArgumentException("이름은 비어 있을 수 없습니다.");
        }
        this.fullName = newName;
    }

    public void addAddress(Address address) {
        addressHistory.add(address);
    }

    public void removeAddress(Address address) {
        addressHistory.remove(address);
    }
}
