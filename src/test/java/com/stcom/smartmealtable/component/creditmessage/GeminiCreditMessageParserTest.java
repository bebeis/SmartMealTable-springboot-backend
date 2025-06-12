package com.stcom.smartmealtable.component.creditmessage;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.chat.client.ChatClient;

class GeminiCreditMessageParserTest {

    private ChatClient.Builder builder;
    private ChatClient chatClient;
    private GeminiCreditMessageParser parser;

    @BeforeEach
    void setUp() {
        // Deep-stub 으로 체이닝 메서드 mock
        chatClient = mock(ChatClient.class, Mockito.RETURNS_DEEP_STUBS);
        builder = mock(ChatClient.Builder.class);
        when(builder.build()).thenReturn(chatClient);

        parser = new GeminiCreditMessageParser(builder);
    }

    @Test
    @DisplayName("LLM JSON 응답을 DTO 로 변환한다")
    void parseSuccess() {
        // given
        String llmJson = "{" +
                "\"vendor\":\"KB\"," +
                "\"dateTime\":\"2025-06-12T10:20:00\"," +
                "\"amount\":11000," +
                "\"tradeName\":\"스타벅스\"}";

        // when
        when(chatClient.prompt().user(anyString()).call().content()).thenReturn(llmJson);

        String sms = "[KB국민카드] 06/12 10:20 승인 11,000원 스타벅스";
        ExpenditureDto dto = parser.parse(sms);

        // then
        assertAll(
                () -> assertEquals("KB", dto.getVendor()),
                () -> assertEquals(11000L, dto.getAmount()),
                () -> assertEquals("스타벅스", dto.getTradeName()),
                () -> assertEquals("2025-06-12T10:20", dto.getSpentDate().toString().substring(0, 16))
        );
    }
}