package com.stcom.smartmealtable.domain.Address;

import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor
public class Address {

    private String lotNumberAddress;

    private String roadAddress;

    private String detailAddress;

    private Double latitude;

    private Double longitude;

    @Builder
    public Address(String lotNumberAddress, String roadAddress, String detailAddress, String alias, Double latitude,
                   Double longitude, AddressType type) {
        this.lotNumberAddress = lotNumberAddress;
        this.roadAddress = roadAddress;
        this.detailAddress = detailAddress;
        this.latitude = latitude;
        this.longitude = longitude;
    }


    public void updateAddress(String lotNumberAddress, String roadAddress, String detailAddress,
                              Double latitude, Double longitude) {
        this.lotNumberAddress = lotNumberAddress;
        this.roadAddress = roadAddress;
        this.detailAddress = detailAddress;
        this.latitude = latitude;
        this.longitude = longitude;
    }


}
