package com.stcom.smartmealtable.domain.Address;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Long id;

    private String lotNumberAddress;

    private String roadAddress;

    private String detailAddress;

    private String alias;

    private Double latitude;

    private Double longitude;

    @Enumerated(EnumType.STRING)
    private AddressType type;

    @Builder
    public Address(String lotNumberAddress, String roadAddress, String detailAddress, String alias, Double latitude,
                   Double longitude, AddressType type) {
        this.lotNumberAddress = lotNumberAddress;
        this.roadAddress = roadAddress;
        this.detailAddress = detailAddress;
        this.alias = alias;
        this.latitude = latitude;
        this.longitude = longitude;
        this.type = type;
    }


    public void updateAddress(String lotNumberAddress, String roadAddress, String detailAddress,
                              Double latitude, Double longitude) {
        this.lotNumberAddress = lotNumberAddress;
        this.roadAddress = roadAddress;
        this.detailAddress = detailAddress;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void changeAddressType(AddressType newType) {
        this.type = newType;
    }
}
