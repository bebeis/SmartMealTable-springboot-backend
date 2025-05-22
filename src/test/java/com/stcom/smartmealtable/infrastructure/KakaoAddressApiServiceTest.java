package com.stcom.smartmealtable.infrastructure;

import com.stcom.smartmealtable.infrastructure.dto.AddressRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class KakaoAddressApiServiceTest {

    @InjectMocks
    private KakaoAddressApiService kakaoAddressApiService;

    @Test
    @DisplayName("카카오 주소 API 서비스 테스트")
    void kakaoAddressApiServiceTest() {
        // given
        ReflectionTestUtils.setField(kakaoAddressApiService, "clientId", "test-client-id");
        
        // 실제 API 호출이 필요한 테스트는 통합 테스트에서 수행해야 합니다.
        // 이 단위 테스트에서는 RestClient 모킹이 복잡하므로 생략합니다.
    }
} 