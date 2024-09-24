package ru.tehnobear.essence.dao.dto;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Arrays;

public enum EAssetType {
    TEXT,
    BINARY;

    @JsonCreator
    public static EAssetType fromText(String search) {
        if (search == null) {
            return BINARY;
        }
        var upperSearch = search.toUpperCase();
        return Arrays.stream(EAssetType.values())
                .filter(val -> val.name().equals(upperSearch))
                .findFirst()
                .orElse(BINARY);
    }
}
