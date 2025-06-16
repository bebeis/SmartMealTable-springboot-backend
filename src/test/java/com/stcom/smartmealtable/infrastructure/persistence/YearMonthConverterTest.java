package com.stcom.smartmealtable.infrastructure.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.time.LocalDate;
import java.time.YearMonth;

import static org.assertj.core.api.Assertions.*;

class YearMonthConverterTest {

    private YearMonthConverter converter;

    @BeforeEach
    void setUp() {
        converter = new YearMonthConverter();
    }

    @Test
    @DisplayName("YearMonth를 Date로 변환 - 정상 케이스")
    void convertToDatabaseColumn_success() {
        // given
        YearMonth yearMonth = YearMonth.of(2024, 12);

        // when
        Date result = converter.convertToDatabaseColumn(yearMonth);

        // then
        assertThat(result).isEqualTo(Date.valueOf(LocalDate.of(2024, 12, 1)));
    }

    @Test
    @DisplayName("YearMonth가 null인 경우 null 반환")
    void convertToDatabaseColumn_nullInput() {
        // when
        Date result = converter.convertToDatabaseColumn(null);

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Date를 YearMonth로 변환 - 정상 케이스")
    void convertToEntityAttribute_success() {
        // given
        Date dbData = Date.valueOf(LocalDate.of(2024, 12, 15));

        // when
        YearMonth result = converter.convertToEntityAttribute(dbData);

        // then
        assertThat(result).isEqualTo(YearMonth.of(2024, 12));
    }

    @Test
    @DisplayName("데이터베이스 값이 null인 경우 null 반환")
    void convertToEntityAttribute_nullInput() {
        // when
        YearMonth result = converter.convertToEntityAttribute(null);

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("YearMonth 양방향 변환 테스트")
    void bidirectionalConversion() {
        // given
        YearMonth original = YearMonth.of(2024, 6);

        // when
        Date dbValue = converter.convertToDatabaseColumn(original);
        YearMonth converted = converter.convertToEntityAttribute(dbValue);

        // then
        assertThat(converted).isEqualTo(original);
    }

    @Test
    @DisplayName("월의 첫번째 날로 변환 확인")
    void convertToDatabaseColumn_firstDayOfMonth() {
        // given
        YearMonth yearMonth = YearMonth.of(2024, 3);

        // when
        Date result = converter.convertToDatabaseColumn(yearMonth);

        // then
        LocalDate expectedDate = LocalDate.of(2024, 3, 1);
        assertThat(result).isEqualTo(Date.valueOf(expectedDate));
    }

    @Test
    @DisplayName("Date의 일자와 상관없이 YearMonth 변환")
    void convertToEntityAttribute_anyDayOfMonth() {
        // given
        Date lastDay = Date.valueOf(LocalDate.of(2024, 2, 29)); // 윤년 2월 마지막날
        Date middleDay = Date.valueOf(LocalDate.of(2024, 2, 15));
        Date firstDay = Date.valueOf(LocalDate.of(2024, 2, 1));

        // when
        YearMonth fromLastDay = converter.convertToEntityAttribute(lastDay);
        YearMonth fromMiddleDay = converter.convertToEntityAttribute(middleDay);
        YearMonth fromFirstDay = converter.convertToEntityAttribute(firstDay);

        // then
        YearMonth expected = YearMonth.of(2024, 2);
        assertThat(fromLastDay).isEqualTo(expected);
        assertThat(fromMiddleDay).isEqualTo(expected);
        assertThat(fromFirstDay).isEqualTo(expected);
    }
} 