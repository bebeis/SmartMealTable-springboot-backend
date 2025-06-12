package com.stcom.smartmealtable.component.creditmessage;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreditMessageManager {

    private final GeminiCreditMessageParser geminiParser;

    private final Map<String, CreditMessageParser> parsers = Map.of(
            "KB", new KBCreditMessageParser(),
            "NH", new NHCreditMessageParser(),
            "SH", new SHCreditMessageParser()
    );

    public ExpenditureDto parseMessage(String message) {
        if (message == null || message.isEmpty()) {
            throw new IllegalArgumentException("메시지가 비어 있습니다.");
        }

        for (CreditMessageParser parser : parsers.values()) {
            if (parser.checkVendor(message)) {
                try {
                    return parser.parse(message);
                } catch (Exception ignore) {
                    // 룰 기반 파싱 실패 – Gemini 로 fallback
                    break;
                }
            }
        }

        return geminiParser.parse(message);
    }
}
