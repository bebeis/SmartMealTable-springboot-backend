package com.stcom.smartmealtable.component.creditmessage;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class KBCreditMessageParserTest {

    KBCreditMessageParser parser = new KBCreditMessageParser();

    @DisplayName("국민은행 결제 메시지인지 판별한다.")
    @Test
    void checkVendor() throws Exception {
        // given
        String kbMessage = "[KB국민카드] 07/16 12:28 승인 11,000원 일시불 롯데시네마 평촌";
        // when & then
        assertThat(parser.checkVendor(kbMessage)).isTrue();
    }

    @DisplayName("잘못된 벤더사의 메시지인지 확인한다.")
    @Test
    void checkVendor2() throws Exception {
        // given
        String illegalMessage = "[우리] 07/16 12:28 승인 11,000원 일시불 롯데시네마 평촌";
        // when & then
        assertThat(parser.checkVendor(illegalMessage)).isFalse();

    }

    @DisplayName("국민은행 결제 메시지를 파싱한다.")
    @Test
    void parse() throws Exception {
        // given
        String kbMessage = "[KB국민카드] 07/16 12:28 승인 11,000원 일시불 롯데시네마 평촌";
        // when
        ExpenditureDto expenditure = parser.parse(kbMessage);
        // then
        assertThat(expenditure.getVendor()).isEqualTo("KB");
        assertThat(expenditure.getDateTime()).isEqualTo(LocalDateTime.of(LocalDateTime.now().getYear(), 7, 16, 12, 28));
        assertThat(expenditure.getAmount()).isEqualTo(11000);
        assertThat(expenditure.getTradeName()).isEqualTo("롯데시네마 평촌");

    }
}