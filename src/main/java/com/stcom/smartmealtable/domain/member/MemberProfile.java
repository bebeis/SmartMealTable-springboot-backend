package com.stcom.smartmealtable.domain.member;

import com.stcom.smartmealtable.common.BaseTimeEntity;
import com.stcom.smartmealtable.domain.common.Address;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
    private Member member;

    @Enumerated(EnumType.STRING)
    private MemberGroup memberGroup;

    private Long groupCode;

    private String nickName;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "member_id")
    private List<Address> addressHistory = new ArrayList<>();

    protected void linkMemberAuth(Member member) {
        this.member = member;
    }

    public void changeNickName(String newNickName) {
        if (newNickName.isBlank()) {
            throw new IllegalArgumentException("이름은 비어 있을 수 없습니다.");
        }
        this.nickName = newNickName;
    }

    public void addAddress(Address address) {
        addressHistory.add(address);
    }

    public void removeAddress(Address address) {
        addressHistory.remove(address);
    }
}
