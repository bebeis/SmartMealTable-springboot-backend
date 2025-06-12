package com.stcom.smartmealtable.component.creditmessage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KBCreditMessageParser implements CreditMessageParser {

    private static final Pattern KB_PATTERN = Pattern.compile(
            // 1: MM/dd, 2: HH:mm, 3: amount, 4: trade name
            "\\[KB국민카드]\\s*(\\d{2}/\\d{2})\\s*(\\d{2}:\\d{2})\\s*승인\\s*([\\d,]+)원\\s*[가-힣A-Za-z]*\\s*(.+)"
    );

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

    @Override
    public boolean checkVendor(String message) {
        // 메시지에 "KB"가 포함되어 있는지 확인
        return message != null && message.contains("KB");
    }

    @Override
    public ExpenditureDto parse(String message) {
        if (message == null) {
            throw new IllegalArgumentException("메시지가 비어있습니다.");
        }

        Matcher matcher = KB_PATTERN.matcher(message);
        if (!matcher.find()) {
            throw new IllegalArgumentException("올바르지 않은 메시지 포맷입니다. " + message);
        }

        String datePart = matcher.group(1);
        String timePart = matcher.group(2);
        String amountPart = matcher.group(3).replace(",", ""); // “11,000” → “11000”
        String tradeName = matcher.group(4).trim();

        int currentYear = LocalDate.now().getYear();
        LocalDateTime dateTime = LocalDateTime.parse(
                currentYear + "/" + datePart + " " + timePart, FORMATTER
        );

        long amount = Long.parseLong(amountPart);

        return new ExpenditureDto("KB", dateTime, amount, tradeName);
    }
}
