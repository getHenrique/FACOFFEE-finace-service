package com.facoffee.financeService.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalDateTime;

/**
 * Persiste LocalDateTime como texto ISO-8601 no SQLite.
 * Evita que o driver sqlite-jdbc grave/leia datas como epoch-millis
 * (o que quebra o FastDateParser na leitura).
 */
@Converter(autoApply = true)
public class LocalDateTimeStringConverter implements AttributeConverter<LocalDateTime, String> {

    @Override
    public String convertToDatabaseColumn(LocalDateTime attribute) {
        return attribute == null ? null : attribute.toString();
    }

    @Override
    public LocalDateTime convertToEntityAttribute(String dbData) {
        return dbData == null ? null : LocalDateTime.parse(dbData);
    }
}
