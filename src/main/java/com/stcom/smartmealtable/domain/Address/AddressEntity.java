package com.stcom.smartmealtable.domain.Address;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "address")
public class AddressEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Long id;

    @Embedded
    private Address address;

    private boolean primary = false;

    @Enumerated(EnumType.STRING)
    private AddressType type;

    private String alias;

    @Builder
    public AddressEntity(Address address, AddressType type, String alias) {
        this.address = address;
        this.type = type;
        this.alias = alias;
    }

    public AddressEntity(Address address) {
        this.address = address;
    }

    public void markPrimary() {
        this.primary = true;
    }

    public void unmarkPrimary() {
        this.primary = false;
    }

    public boolean isPrimaryAddress() {
        return primary;
    }

    public void changeAddressType(AddressType newType) {
        this.type = newType;
    }

    public void changeAlias(String newAlias) {
        this.alias = newAlias;
    }


}
