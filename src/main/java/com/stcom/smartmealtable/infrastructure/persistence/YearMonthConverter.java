package com.stcom.smartmealtable.infrastructure.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.sql.Date;
import java.time.YearMonth;

@Converter(autoApply = true)
public class YearMonthConverter implements AttributeConverter<YearMonth, Date> {

    @Override
    public Date convertToDatabaseColumn(YearMonth attribute) {
        return (attribute == null ? null : Date.valueOf(attribute.atDay(1)));
    }

    @Override
    public YearMonth convertToEntityAttribute(Date dbData) {
        return (dbData == null ? null : YearMonth.from(dbData.toLocalDate()));
    }
}
