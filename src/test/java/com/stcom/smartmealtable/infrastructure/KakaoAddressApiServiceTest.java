package com.stcom.smartmealtable.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.stcom.smartmealtable.domain.Address.Address;
import com.stcom.smartmealtable.exception.ExternApiStatusError;
import com.stcom.smartmealtable.infrastructure.dto.AddressRequest;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.RequestHeadersSpec;
import org.springframework.web.client.RestClient.RequestHeadersUriSpec;
import org.springframework.web.client.RestClient.ResponseSpec;

@SuppressWarnings("unchecked")
class KakaoAddressApiServiceTest {

    private KakaoAddressApiService kakaoAddressApiService;
    private RestClient mockClient;
    private RequestHeadersUriSpec<?> uriSpec;
    private RequestHeadersSpec<?> headersSpec;
    private ResponseSpec responseSpec;

    @BeforeEach
    void setUp() {
        kakaoAddressApiService = new KakaoAddressApiService();
        mockClient = Mockito.mock(RestClient.class);
        uriSpec = Mockito.mock(RequestHeadersUriSpec.class);
        headersSpec = Mockito.mock(RequestHeadersSpec.class);
        responseSpec = Mockito.mock(ResponseSpec.class);

        // stub common chain
        Mockito.when(mockClient.get()).thenAnswer(invocation -> uriSpec);
        Mockito.when((uriSpec).uri(Mockito.any(Function.class))).thenAnswer(invocation -> headersSpec);
        Mockito.when(headersSpec.header(Mockito.eq("Authorization"), Mockito.anyString())).thenAnswer(invocation -> headersSpec);
        
        ReflectionTestUtils.setField(kakaoAddressApiService, "client", mockClient);
        ReflectionTestUtils.setField(kakaoAddressApiService, "clientId", "testKey");
    }

    @DisplayName("정상적으로 주소를 생성한다")
    @Test
    void createAddressFromRequest_success() {
        // given
        KakaoAddressApiService.Meta meta = new KakaoAddressApiService.Meta(1, 1, true);
        KakaoAddressApiService.LotAddress lotAddress = new KakaoAddressApiService.LotAddress(
                "lotaddr", "reg1", "reg2", "reg3", "reg3H", "hCode", "bCode", "N",
                "123", "4", "127.123", "37.123");
        KakaoAddressApiService.RoadAddress roadAddress = new KakaoAddressApiService.RoadAddress(
                "roadaddr", "reg1", "reg2", "reg3", "road", "N", "1", "2", "building",
                "12345", "127.123", "37.123");
        KakaoAddressApiService.Document document = new KakaoAddressApiService.Document(
                "address", "type", "127.123", "37.123", lotAddress, roadAddress);
        KakaoAddressApiService.AddressSearchResponse response = new KakaoAddressApiService.AddressSearchResponse(
                meta, List.of(document));

        Mockito.when(headersSpec.retrieve()).thenReturn(responseSpec);
        Mockito.when(responseSpec.body(KakaoAddressApiService.AddressSearchResponse.class))
                .thenReturn(response);

        AddressRequest request = new AddressRequest("roadaddr", "detailaddr");

        // when
        Address result = kakaoAddressApiService.createAddressFromRequest(request);

        // then
        assertEquals(127.123, result.getLongitude());
        assertEquals(37.123, result.getLatitude());
        assertEquals("lotaddr", result.getLotNumberAddress());
        assertEquals("roadaddr", result.getRoadAddress());
        assertEquals("detailaddr", result.getDetailAddress());
    }

    @DisplayName("외부 API 호출 실패 시 ExternApiStatusError를 던진다")
    @Test
    void createAddressFromRequest_fail() {
        // given
        Mockito.when(headersSpec.retrieve()).thenThrow(new RuntimeException("connection error"));

        AddressRequest request = new AddressRequest("roadaddr", "detailaddr");

        // when & then
        assertThrows(ExternApiStatusError.class, () -> kakaoAddressApiService.createAddressFromRequest(request));
    }
} 