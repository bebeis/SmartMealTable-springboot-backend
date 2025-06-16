package com.stcom.smartmealtable.component.creditmessage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.chat.client.ChatClient;

class CreditMessageManagerTest {

    private ChatClient.Builder builder;
    private ChatClient chatClient;
    private GeminiCreditMessageParser geminiParser;
    private CreditMessageManager manager;

    @BeforeEach
    void setUp() {
        // LLM 호출 모킹을 위한 deep stub
        chatClient = mock(ChatClient.class, Mockito.RETURNS_DEEP_STUBS);
        builder = mock(ChatClient.Builder.class);
        when(builder.build()).thenReturn(chatClient);

        geminiParser = spy(new GeminiCreditMessageParser(builder));
        manager = new CreditMessageManager(geminiParser);
    }

    @Test
    @DisplayName("KB 메시지는 룰 파서가 처리하고 Gemini 는 호출되지 않는다")
    void ruleBasedParsingWorks() {
        // given
        String sms = "[KB국민카드] 06/12 10:20 승인 11,000원 스타벅스";

        // when
        ExpenditureDto dto = manager.parseMessage(sms);

        // then
        assertEquals("KB", dto.getVendor());
        assertEquals(11000L, dto.getAmount());
        verify(geminiParser, never()).parse(anyString());
    }

    @Test
    @DisplayName("룰 파서 실패 시 Gemini fallback 이 동작한다")
    void fallbackToGemini() {
        // given
        String llmJson = "{" +
                "\"vendor\":\"UNKNOWN\"," +
                "\"dateTime\":\"2025-06-12T10:20:00\"," +
                "\"amount\":5000," +
                "\"tradeName\":\"GS25\"}";

        // when
        when(chatClient.prompt().user(anyString()).call().content()).thenReturn(llmJson);

        String sms = "알 수 없는 카드사 메시지";
        ExpenditureDto dto = manager.parseMessage(sms);

        // then
        assertEquals("UNKNOWN", dto.getVendor());
        assertEquals(5000L, dto.getAmount());
        verify(geminiParser, times(1)).parse(anyString());
    }
} 