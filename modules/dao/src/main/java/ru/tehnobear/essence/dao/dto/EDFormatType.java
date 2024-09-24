package ru.tehnobear.essence.dao.dto;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Arrays;

public enum EDFormatType {
    JRXML,
    PLUGIN;

    @JsonCreator
    public static EDFormatType fromText(String search) {
        if (search == null) {
            return JRXML;
        }
        var upperSearch = search.toUpperCase();
        return Arrays.stream(EDFormatType.values())
                .filter(val -> val.name().equals(upperSearch))
                .findFirst()
                .orElse(JRXML);
    }
}
