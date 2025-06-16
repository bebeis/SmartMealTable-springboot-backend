package com.stcom.smartmealtable.component.creditmessage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.Builder;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class GeminiCreditMessageParser implements CreditMessageParser {

    private final Builder chatClientBuilder;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public boolean checkVendor(String message) {
        return true;
    }

    @Override
    public ExpenditureDto parse(String message) {
        ChatClient chatClient = chatClientBuilder.build();

        String prompt = String.format("""
                너는 대한민국의 신용카드 승인 문자(SMS)를 파싱해서 JSON 형태로 반환하는 전문가야.
                반드시 아래 형식의 JSON 만 출력해. 설명 문구나 코드 블록 표시(```)는 절대 포함하면 안 돼.
                
                {
                  \"vendor\": \"<카드사 영문 약어, 예: KB, NH, SH, UNKNOWN>\",
                  \"dateTime\": \"<ISO-8601 형식 yyyy-MM-dd'T'HH:mm:ss>\",
                  \"amount\": <숫자형 원화 금액>,
                  \"tradeName\": \"<가맹점명>\"
                }
                
                다음은 파싱 대상 SMS 원문이다:
                %s
                """, message);

        String jsonResponse = chatClient.prompt()
                .user(prompt)
                .call()
                .content();
        try {
            JsonNode root = MAPPER.readTree(jsonResponse);
            String vendor = root.path("vendor").asText("UNKNOWN");
            String dateTimeStr = root.path("dateTime").asText();
            long amount = root.path("amount").asLong();
            String tradeName = root.path("tradeName").asText();

            LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr);
            return new ExpenditureDto(vendor, dateTime, amount, tradeName);
        } catch (Exception e) {
            throw new IllegalArgumentException("Gemini 파싱 실패: " + e.getMessage(), e);
        }
    }
} 