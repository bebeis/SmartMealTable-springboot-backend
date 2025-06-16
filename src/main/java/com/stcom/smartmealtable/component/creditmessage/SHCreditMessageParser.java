package com.stcom.smartmealtable.component.creditmessage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SHCreditMessageParser implements CreditMessageParser {

    private static final Pattern SH_PATTERN = Pattern.compile(
            "신한카드.*?승인.*?([\\d,]+)원(?:\\([^)]*\\))?\\s*(\\d{2}/\\d{2})\\s*(\\d{2}:\\d{2})\\s+(.+?)(?:\\s+(?:누적|잔여).*)?$"
    );

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

    @Override
    public boolean checkVendor(String message) {
        return message != null && message.contains("신한카드");
    }

    @Override
    public ExpenditureDto parse(String message) {
        if (message == null) {
            throw new IllegalArgumentException("메시지가 비어 있습니다.");
        }

        Matcher m = SH_PATTERN.matcher(message);
        if (!m.find()) {
            throw new IllegalArgumentException("신한카드 SMS 형식을 인식하지 못했습니다: " + message);
        }

        long amount = Long.parseLong(m.group(1).replace(",", ""));
        int year = LocalDate.now().getYear();
        LocalDateTime dateTime = LocalDateTime.parse(
                year + "/" + m.group(2) + " " + m.group(3), FMT
        );

        String tradeName = m.group(4).trim()
                .replaceAll("\\s*(누적|잔여|잔액).*", "") // 혹시 남은 잔여표기가 끼어들면 제거
                .trim();

        return new ExpenditureDto("SH", dateTime, amount, tradeName);
    }
}
