package com.stcom.smartmealtable.web.controller;

import com.stcom.smartmealtable.domain.Address.Address;
import com.stcom.smartmealtable.domain.Address.AddressType;
import com.stcom.smartmealtable.infrastructure.AddressApiService;
import com.stcom.smartmealtable.infrastructure.dto.AddressRequest;
import com.stcom.smartmealtable.service.MemberProfileService;
import com.stcom.smartmealtable.service.dto.MemberDto;
import com.stcom.smartmealtable.web.argumentresolver.UserContext;
import com.stcom.smartmealtable.web.dto.ApiResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members/me/addresses")
public class MemberAddressController {

    private final MemberProfileService memberProfileService;
    private final AddressApiService addressApiService;

    @PostMapping("/{id}/primary")
    public ApiResponse<Void> changePrimaryAddress(@UserContext MemberDto memberDto,
                                                  @PathVariable("id") Long addressId) {
        memberProfileService.changeAddressToPrimary(memberDto.getProfileId(), addressId);
        return ApiResponse.createSuccessWithNoContent();
    }

    @PostMapping
    public ApiResponse<Void> registerAddress(@UserContext MemberDto memberDto,
                                             @Validated @RequestBody MemberAddressCURequest request) {
        Address address = addressApiService.createAddressFromRequest(request.toAddressApiRequest());
        memberProfileService.saveNewAddress(memberDto.getProfileId(), address, request.getAlias(),
                request.getAddressType());
        return ApiResponse.createSuccessWithNoContent();
    }

    @PatchMapping("/{id}")
    public ApiResponse<Void> changeAddress(@UserContext MemberDto memberDto,
                                           @PathVariable("id") Long addressId,
                                           @Validated @RequestBody MemberAddressCURequest request) {
        Address address = addressApiService.createAddressFromRequest(request.toAddressApiRequest());
        memberProfileService.changeAddress(memberDto.getProfileId(), addressId, address, request.getAlias(),
                request.getAddressType());
        return ApiResponse.createSuccessWithNoContent();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteAddress(@UserContext MemberDto memberDto,
                                           @PathVariable("id") Long addressId) {
        memberProfileService.deleteAddress(memberDto.getProfileId(), addressId);
        return ApiResponse.createSuccessWithNoContent();
    }

    @AllArgsConstructor
    @Data
    static class MemberAddressCURequest {
        private String roadAddress;
        private AddressType addressType;
        private String alias;
        private String detailAddress;

        public AddressRequest toAddressApiRequest() {
            return new AddressRequest(roadAddress, detailAddress);
        }
    }
} 