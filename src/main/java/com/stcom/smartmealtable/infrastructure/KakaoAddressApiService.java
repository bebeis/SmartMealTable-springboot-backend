package com.stcom.smartmealtable.infrastructure;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.stcom.smartmealtable.domain.Address.Address;
import com.stcom.smartmealtable.exception.ExternApiStatusError;
import com.stcom.smartmealtable.infrastructure.dto.AddressRequest;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class KakaoAddressApiService implements AddressApiService {

    @Value("${kakao.oauth.client-id}")
    private String clientId;

    private final RestClient client = RestClient.create();

    public Address createAddressFromRequest(AddressRequest requestDto) {
        try {
            AddressSearchResponse addressSearchResponse = client.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("dapi.kakao.com")
                            .path("/v2/local/search/address")
                            .queryParam("query", requestDto.getRoadAddress())
                            .build())
                    .header("Authorization", "KakaoAK " + clientId)
                    .retrieve()
                    .body(AddressSearchResponse.class);
            validateAddressSearchResponse(addressSearchResponse);
            return Address.builder()
                    .longitude(Double.parseDouble(addressSearchResponse.getDocuments().getFirst().getLongitude()))
                    .latitude(Double.parseDouble(addressSearchResponse.getDocuments().getFirst().getLatitude()))
                    .lotNumberAddress(addressSearchResponse.getDocuments().getFirst().getAddress().getAddressName())
                    .roadAddress(addressSearchResponse.getDocuments().getFirst().getRoadAddress().getAddressName())
                    .detailAddress(requestDto.getDetailAddress())
                    .build();

        } catch (RuntimeException e) {
            throw new ExternApiStatusError("카카오 주소 Api 호출 중 오류가 발생했습니다.", e);
        }
    }

    private void validateAddressSearchResponse(AddressSearchResponse addressSearchResponse) {
        if (addressSearchResponse == null) {
            throw new IllegalArgumentException("조회된 결과가 없습니다");
        }

        if (addressSearchResponse.getMeta().getTotalCount() >= 2) {
            throw new IllegalArgumentException("주소가 모호합니다. 정확한 주소를 입력하세요");
        }
    }

    @Data
    @AllArgsConstructor
    static class AddressSearchResponse {
        private Meta meta;
        private List<Document> documents;
    }

    @Data
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    static class Meta {
        private Integer totalCount;
        private Integer pageableCount;
        private Boolean isEnd;
    }

    @Data
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    static class Document {
        private String addressName;

        private String addressType;

        @JsonProperty("x")
        private String longitude;

        @JsonProperty("y")
        private String latitude;

        private LotAddress address;

        private RoadAddress roadAddress;

    }

    @Data
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    static class LotAddress {
        private String addressName;

        @JsonProperty("region_1depth_name")
        private String region1depthName;

        @JsonProperty("region_2depth_name")
        private String region2depthName;

        @JsonProperty("region_3depth_name")
        private String region3depthName;

        @JsonProperty("region_3depth_h_name")
        private String region3depthHName;

        private String hCode;

        private String bCode;

        private String mountainYn;

        private String mainAddressNo;

        private String subAddressNo;

        private String x;
        private String y;
    }

    @Data
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    static class RoadAddress {
        private String addressName;

        @JsonProperty("region_1depth_name")
        private String region1depthName;

        @JsonProperty("region_2depth_name")
        private String region2depthName;

        @JsonProperty("region_3depth_name")
        private String region3depthName;

        private String roadName;

        @JsonProperty("underground_yn")
        private String undergroundYn;

        @JsonProperty("main_building_no")
        private String mainBuildingNo;

        @JsonProperty("sub_building_no")
        private String subBuildingNo;

        @JsonProperty("building_name")
        private String buildingName;

        @JsonProperty("zone_no")
        private String zoneNo;

        private String x;
        private String y;
    }
}
