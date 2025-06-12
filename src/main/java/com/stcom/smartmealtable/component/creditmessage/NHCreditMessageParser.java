package com.stcom.smartmealtable.component.creditmessage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NHCreditMessageParser implements CreditMessageParser {

    private static final Pattern NH_PATTERN = Pattern.compile(
            "NH(?:농협)?카드.*?승인.*?([\\d,]+)원.*?(\\d{2}/\\d{2})\\s*(\\d{2}:\\d{2})\\s+(.+?)(?:\\s+(?:총누적|잔여).*)?$"
    );

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

    @Override
    public boolean checkVendor(String message) {
        return message != null && (message.contains("NH") || message.contains("농협"));
    }

    @Override
    public ExpenditureDto parse(String message) {
        if (message == null) {
            throw new IllegalArgumentException("메시지가 비어 있습니다.");
        }

        Matcher m = NH_PATTERN.matcher(message);
        if (!m.find()) {
            throw new IllegalArgumentException("농협카드 SMS 형식을 인식하지 못했습니다: " + message);
        }

        long amount = Long.parseLong(m.group(1).replace(",", ""));
        int thisYear = LocalDate.now().getYear();
        LocalDateTime dateTime = LocalDateTime.parse(
                thisYear + "/" + m.group(2) + " " + m.group(3), FMT
        );

        String trade = m.group(4).trim();

        return new ExpenditureDto("NH", dateTime, amount, trade);
    }
}
