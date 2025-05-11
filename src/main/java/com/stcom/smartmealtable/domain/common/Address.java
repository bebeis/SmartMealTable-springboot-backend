package com.stcom.smartmealtable.domain.common;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import java.math.BigDecimal;
import lombok.Getter;

@Entity
@Getter
public class Address {

    @Id
    @GeneratedValue
    @Column(name = "address_id")
    private Long id;

    private String lotNumberAddress;

    private String roadAddress;

    private String detailAddress;

    private String alias;

    private BigDecimal latitude;

    private BigDecimal longitude;

    @Enumerated(EnumType.STRING)
    private AddressType type;

    public void updateAddress(String lotNumberAddress, String roadAddress, String detailAddress,
                              BigDecimal latitude, BigDecimal longitude) {
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
