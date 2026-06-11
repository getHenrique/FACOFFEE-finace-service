package com.facoffee.financeService.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalDate;

/**
 * Persiste LocalDate como texto ISO-8601 (yyyy-MM-dd) no SQLite.
 * Evita que o driver sqlite-jdbc grave/leia datas como epoch-millis
 * (o que quebra o FastDateParser na leitura).
 */
@Converter(autoApply = true)
public class LocalDateStringConverter implements AttributeConverter<LocalDate, String> {

    @Override
    public String convertToDatabaseColumn(LocalDate attribute) {
        return attribute == null ? null : attribute.toString();
    }

    @Override
    public LocalDate convertToEntityAttribute(String dbData) {
        return dbData == null ? null : LocalDate.parse(dbData);
    }
}
