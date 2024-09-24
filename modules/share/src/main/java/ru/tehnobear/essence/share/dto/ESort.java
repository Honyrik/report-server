package ru.tehnobear.essence.share.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.querydsl.core.types.Order;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum ESort {
    ASC,
    DESC;

    @JsonCreator
    public static ESort fromText(String search) {
        if (search == null) {
            return ASC;
        }
        var searchUpper = search.toUpperCase();
        return Arrays.stream(ESort.values())
                .filter(val -> val.name().equals(searchUpper))
                .findFirst()
                .orElse(ASC);
    }

    public Order toOrder() {
        return Arrays.stream(Order.values())
                .filter(val -> val.name().equalsIgnoreCase(this.name()))
                .findFirst()
                .orElse(Order.ASC);
    }
}
