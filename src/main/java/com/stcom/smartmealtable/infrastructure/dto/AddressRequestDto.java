package com.stcom.smartmealtable.infrastructure.dto;

import com.stcom.smartmealtable.domain.Address.AddressType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AddressRequestDto {

    private String roadAddress;

    private AddressType addressType;

    private String alias;

    private String detailAddress;
}
