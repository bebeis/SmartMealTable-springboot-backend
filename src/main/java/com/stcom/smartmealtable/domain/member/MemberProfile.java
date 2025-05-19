package com.stcom.smartmealtable.domain.member;

import com.stcom.smartmealtable.domain.Address.Address;
import com.stcom.smartmealtable.domain.common.BaseTimeEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class MemberProfile extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_profile_id")
    private Long id;

    @OneToOne(mappedBy = "memberProfile", orphanRemoval = true)
    private Member member;

    @Enumerated(EnumType.STRING)
    private MemberGroup memberGroup;

    private String groupName;

    private String nickName;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "member_id")
    private List<Address> addressHistory = new ArrayList<>();

    @Builder
    public MemberProfile(Member member, MemberGroup memberGroup, String groupName, String nickName,
                         List<Address> addressHistory) {
        this.member = member;
        this.memberGroup = memberGroup;
        this.groupName = groupName;
        this.nickName = nickName;
        this.addressHistory = addressHistory;
    }

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
