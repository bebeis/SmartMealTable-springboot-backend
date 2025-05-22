package com.stcom.smartmealtable.domain.Address;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AddressTest {

    @Test
    @DisplayName("Address 빌더를 통해 객체가 올바르게 생성된다")
    void addressIsCreatedCorrectlyUsingBuilder() {
        // given & when
        Address address = Address.builder()
                .lotNumberAddress("서울특별시 강남구 역삼동 123-45")
                .roadAddress("서울특별시 강남구 테헤란로 123")
                .detailAddress("4층 401호")
                .latitude(37.5012)
                .longitude(127.0396)
                .build();
        
        // then
        assertThat(address.getLotNumberAddress()).isEqualTo("서울특별시 강남구 역삼동 123-45");
        assertThat(address.getRoadAddress()).isEqualTo("서울특별시 강남구 테헤란로 123");
        assertThat(address.getDetailAddress()).isEqualTo("4층 401호");
        assertThat(address.getLatitude()).isEqualTo(37.5012);
        assertThat(address.getLongitude()).isEqualTo(127.0396);
    }
    
    @Test
    @DisplayName("updateAddress 메소드로 주소 정보를 업데이트할 수 있다")
    void updateAddressMethodUpdatesAddressInformation() {
        // given
        Address address = Address.builder()
                .lotNumberAddress("서울특별시 강남구 역삼동 123-45")
                .roadAddress("서울특별시 강남구 테헤란로 123")
                .detailAddress("4층 401호")
                .latitude(37.5012)
                .longitude(127.0396)
                .build();
        
        // when
        address.updateAddress(
                "서울특별시 서초구 서초동 987-65",
                "서울특별시 서초구 서초대로 456",
                "8층 802호",
                37.4923,
                127.0292
        );
        
        // then
        assertThat(address.getLotNumberAddress()).isEqualTo("서울특별시 서초구 서초동 987-65");
        assertThat(address.getRoadAddress()).isEqualTo("서울특별시 서초구 서초대로 456");
        assertThat(address.getDetailAddress()).isEqualTo("8층 802호");
        assertThat(address.getLatitude()).isEqualTo(37.4923);
        assertThat(address.getLongitude()).isEqualTo(127.0292);
    }
    
    @Test
    @DisplayName("AddressType enum 값이 올바르게 정의되어 있다")
    void addressTypeEnumHasCorrectValues() {
        // given
        AddressType[] addressTypes = AddressType.values();
        
        // when & then
        assertThat(addressTypes).hasSize(4);
        assertThat(addressTypes).contains(
                AddressType.HOME,
                AddressType.SCHOOL,
                AddressType.OFFICE,
                AddressType.ETC
        );
    }
} 