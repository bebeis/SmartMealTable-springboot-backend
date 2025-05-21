package com.stcom.smartmealtable.infrastructure;

import com.stcom.smartmealtable.domain.Address.Address;
import com.stcom.smartmealtable.infrastructure.dto.AddressRequest;

public interface AddressApiService {

    Address createAddressFromRequest(AddressRequest requestDto);
}
