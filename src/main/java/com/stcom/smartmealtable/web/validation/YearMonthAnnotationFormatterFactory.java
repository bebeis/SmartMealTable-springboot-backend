package com.stcom.smartmealtable.web.validation;

import java.text.ParseException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.format.AnnotationFormatterFactory;
import org.springframework.format.Parser;
import org.springframework.format.Printer;

public class YearMonthAnnotationFormatterFactory implements AnnotationFormatterFactory<YearMonthFormat> {

    private static final Set<Class<?>> FIELD_TYPES =
            Set.copyOf(List.of(YearMonth.class));

    @Override
    public Set<Class<?>> getFieldTypes() {
        return FIELD_TYPES;
    }

    @Override
    public Printer<?> getPrinter(YearMonthFormat annotation, Class<?> fieldType) {
        return new YearMonthFormatter(annotation.pattern());
    }

    @Override
    public Parser<?> getParser(YearMonthFormat annotation, Class<?> fieldType) {
        return new YearMonthFormatter(annotation.pattern());
    }

    private static class YearMonthFormatter implements Printer<YearMonth>, Parser<YearMonth> {
        private final DateTimeFormatter formatter;

        public YearMonthFormatter(String pattern) {
            this.formatter = DateTimeFormatter.ofPattern(pattern);
        }

        @Override
        public String print(YearMonth yearMonth, Locale locale) {
            return yearMonth.format(formatter);
        }

        @Override
        public YearMonth parse(String text, Locale locale) throws ParseException {
            return YearMonth.parse(text, formatter);
        }
    }
}
