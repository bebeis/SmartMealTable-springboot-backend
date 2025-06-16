package com.stcom.smartmealtable.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AddressRequest {

    private String roadAddress;

    private String detailAddress;
}
