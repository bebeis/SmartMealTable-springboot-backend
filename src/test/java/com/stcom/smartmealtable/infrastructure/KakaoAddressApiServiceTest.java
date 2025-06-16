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

    @DisplayName("조회 결과가 null인 경우 IllegalArgumentException을 던진다")
    @Test
    void createAddressFromRequest_nullResponse() {
        // given
        Mockito.when(headersSpec.retrieve()).thenReturn(responseSpec);
        Mockito.when(responseSpec.body(KakaoAddressApiService.AddressSearchResponse.class))
                .thenReturn(null);

        AddressRequest request = new AddressRequest("roadaddr", "detailaddr");

        // when & then
        ExternApiStatusError exception = assertThrows(ExternApiStatusError.class, 
                () -> kakaoAddressApiService.createAddressFromRequest(request));
        assertEquals("카카오 주소 Api 호출 중 오류가 발생했습니다.", exception.getMessage());
    }

    @DisplayName("조회 결과가 2개 이상인 경우 IllegalArgumentException을 던진다")
    @Test
    void createAddressFromRequest_ambiguousAddress() {
        // given
        KakaoAddressApiService.Meta meta = new KakaoAddressApiService.Meta(2, 2, true);
        KakaoAddressApiService.LotAddress lotAddress = new KakaoAddressApiService.LotAddress(
                "lotaddr", "reg1", "reg2", "reg3", "reg3H", "hCode", "bCode", "N",
                "123", "4", "127.123", "37.123");
        KakaoAddressApiService.RoadAddress roadAddress = new KakaoAddressApiService.RoadAddress(
                "roadaddr", "reg1", "reg2", "reg3", "road", "N", "1", "2", "building",
                "12345", "127.123", "37.123");
        KakaoAddressApiService.Document document = new KakaoAddressApiService.Document(
                "address", "type", "127.123", "37.123", lotAddress, roadAddress);
        KakaoAddressApiService.AddressSearchResponse response = new KakaoAddressApiService.AddressSearchResponse(
                meta, List.of(document, document));

        Mockito.when(headersSpec.retrieve()).thenReturn(responseSpec);
        Mockito.when(responseSpec.body(KakaoAddressApiService.AddressSearchResponse.class))
                .thenReturn(response);

        AddressRequest request = new AddressRequest("roadaddr", "detailaddr");

        // when & then
        ExternApiStatusError exception = assertThrows(ExternApiStatusError.class, 
                () -> kakaoAddressApiService.createAddressFromRequest(request));
        assertEquals("카카오 주소 Api 호출 중 오류가 발생했습니다.", exception.getMessage());
    }

    @DisplayName("조회 결과가 0개인 경우 정상 처리된다")
    @Test
    void createAddressFromRequest_noResults() {
        // given
        KakaoAddressApiService.Meta meta = new KakaoAddressApiService.Meta(0, 0, true);
        KakaoAddressApiService.AddressSearchResponse response = new KakaoAddressApiService.AddressSearchResponse(
                meta, List.of());

        Mockito.when(headersSpec.retrieve()).thenReturn(responseSpec);
        Mockito.when(responseSpec.body(KakaoAddressApiService.AddressSearchResponse.class))
                .thenReturn(response);

        AddressRequest request = new AddressRequest("roadaddr", "detailaddr");

        // when & then
        assertThrows(ExternApiStatusError.class, 
                () -> kakaoAddressApiService.createAddressFromRequest(request));
    }

    @DisplayName("좌표값이 문자열로 전달되어도 정상 변환된다")
    @Test
    void createAddressFromRequest_stringCoordinates() {
        // given
        KakaoAddressApiService.Meta meta = new KakaoAddressApiService.Meta(1, 1, true);
        KakaoAddressApiService.LotAddress lotAddress = new KakaoAddressApiService.LotAddress(
                "서울특별시 강남구 역삼동 123-4", "서울특별시", "강남구", "역삼동", "", 
                "1168010500", "1168010500", "N", "123", "4", "127.033333", "37.500000");
        KakaoAddressApiService.RoadAddress roadAddress = new KakaoAddressApiService.RoadAddress(
                "서울특별시 강남구 테헤란로 123", "서울특별시", "강남구", "역삼동", "테헤란로", 
                "N", "123", "", "타워빌딩", "06142", "127.033333", "37.500000");
        KakaoAddressApiService.Document document = new KakaoAddressApiService.Document(
                "서울특별시 강남구 역삼동 123-4", "ROAD_ADDR", "127.033333", "37.500000", 
                lotAddress, roadAddress);
        KakaoAddressApiService.AddressSearchResponse response = new KakaoAddressApiService.AddressSearchResponse(
                meta, List.of(document));

        Mockito.when(headersSpec.retrieve()).thenReturn(responseSpec);
        Mockito.when(responseSpec.body(KakaoAddressApiService.AddressSearchResponse.class))
                .thenReturn(response);

        AddressRequest request = new AddressRequest("서울특별시 강남구 테헤란로 123", "101호");

        // when
        Address result = kakaoAddressApiService.createAddressFromRequest(request);

        // then
        assertEquals(127.033333, result.getLongitude());
        assertEquals(37.500000, result.getLatitude());
        assertEquals("서울특별시 강남구 역삼동 123-4", result.getLotNumberAddress());
        assertEquals("서울특별시 강남구 테헤란로 123", result.getRoadAddress());
        assertEquals("101호", result.getDetailAddress());
    }

    @DisplayName("숫자 변환 오류 시 ExternApiStatusError를 던진다")
    @Test
    void createAddressFromRequest_invalidCoordinates() {
        // given
        KakaoAddressApiService.Meta meta = new KakaoAddressApiService.Meta(1, 1, true);
        KakaoAddressApiService.LotAddress lotAddress = new KakaoAddressApiService.LotAddress(
                "lotaddr", "reg1", "reg2", "reg3", "reg3H", "hCode", "bCode", "N",
                "123", "4", "invalid", "invalid");
        KakaoAddressApiService.RoadAddress roadAddress = new KakaoAddressApiService.RoadAddress(
                "roadaddr", "reg1", "reg2", "reg3", "road", "N", "1", "2", "building",
                "12345", "invalid", "invalid");
        KakaoAddressApiService.Document document = new KakaoAddressApiService.Document(
                "address", "type", "invalid", "invalid", lotAddress, roadAddress);
        KakaoAddressApiService.AddressSearchResponse response = new KakaoAddressApiService.AddressSearchResponse(
                meta, List.of(document));

        Mockito.when(headersSpec.retrieve()).thenReturn(responseSpec);
        Mockito.when(responseSpec.body(KakaoAddressApiService.AddressSearchResponse.class))
                .thenReturn(response);

        AddressRequest request = new AddressRequest("roadaddr", "detailaddr");

        // when & then
        ExternApiStatusError exception = assertThrows(ExternApiStatusError.class, 
                () -> kakaoAddressApiService.createAddressFromRequest(request));
        assertEquals("카카오 주소 Api 호출 중 오류가 발생했습니다.", exception.getMessage());
    }
} 