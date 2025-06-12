package com.stcom.smartmealtable.component.creditmessage;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SHCreditMessageParserTest {

    SHCreditMessageParser parser = new SHCreditMessageParser();

    @DisplayName("농협 결제 메시지인지 판별한다.")
    @Test
    void checkVendor() throws Exception {
        // given
        String shMessage = "신한카드(6193)승인 가나다 5,700원(일시불)10/21 08:33 (주)티머니 개인택 누적1,000,000원";
        // when & then
        assertThat(parser.checkVendor(shMessage)).isTrue();
    }

    @DisplayName("잘못된 벤더사의 메시지인지 확인한다.")
    @Test
    void checkVendor2() throws Exception {
        // given
        String illegalMessage = "[우리] 07/16 12:28 승인 11,000원 일시불 롯데시네마 평촌";
        // when & then
        assertThat(parser.checkVendor(illegalMessage)).isFalse();

    }

    @DisplayName("농협 결제 메시지를 파싱한다.")
    @Test
    void parse() throws Exception {
        // given
        String shMessage = "신한카드(6193)승인 가나다 5,700원(일시불)10/21 08:33 (주)티머니 개인택 누적1,000,000원";
        // when
        ExpenditureDto expenditure = parser.parse(shMessage);
        // then
        assertThat(expenditure.getVendor()).isEqualTo("SH");
        assertThat(expenditure.getSpentDate()).isEqualTo(
                LocalDateTime.of(LocalDateTime.now().getYear(), 10, 21, 8, 33));
        assertThat(expenditure.getAmount()).isEqualTo(5700);
        assertThat(expenditure.getTradeName()).isEqualTo("(주)티머니 개인택");

    }

}