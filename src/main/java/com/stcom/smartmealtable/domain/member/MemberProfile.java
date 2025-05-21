package com.stcom.smartmealtable.domain.member;

import com.stcom.smartmealtable.domain.Address.Address;
import com.stcom.smartmealtable.domain.Address.AddressEntity;
import com.stcom.smartmealtable.domain.common.BaseTimeEntity;
import com.stcom.smartmealtable.domain.group.Group;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

    @OneToOne(fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "member_id")
    private Member member;

    private String nickName;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "member_id")
    private List<AddressEntity> addressHistory = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    @Builder
    public MemberProfile(Member member, String nickName, List<AddressEntity> addressHistory, Group group) {
        linkMember(member);
        this.nickName = nickName;
        this.addressHistory = addressHistory;
        this.group = group;
    }

    public void addAddress(Address address) {
        addressHistory.add(new AddressEntity(address));
    }

    public void removeAddress(AddressEntity addressEntity) {
        addressHistory.remove(addressEntity);
    }

    public AddressEntity findPrimaryAddress() {
        return addressHistory.stream()
                .filter(AddressEntity::isPrimaryAddress)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Primary Address가 없습니다"));
    }

    public void linkMember(Member member) {
        member.linkMemberProfile(this);
        this.member = member;
    }

    public void changeNickName(String newNickName) {
        if (newNickName.isBlank()) {
            throw new IllegalArgumentException("이름은 비어 있을 수 없습니다.");
        }
        this.nickName = newNickName;
    }

    public void setPrimaryAddress(AddressEntity target) {
        addressHistory.forEach(AddressEntity::unmarkPrimary);
        target.markPrimary();
    }
}
