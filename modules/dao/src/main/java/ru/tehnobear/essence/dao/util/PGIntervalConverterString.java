package ru.tehnobear.essence.dao.util;

import jakarta.persistence.AttributeConverter;
import org.postgresql.util.PGInterval;

import java.sql.SQLException;

public class PGIntervalConverterString implements AttributeConverter<PGInterval, String> {
    @Override
    public String convertToDatabaseColumn(PGInterval attribute) {
        return attribute.toString();
    }

    @Override
    public PGInterval convertToEntityAttribute(String dbData) {
        try {
            return new PGInterval(dbData);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
