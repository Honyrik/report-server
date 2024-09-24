package ru.tehnobear.essence.share.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;


@Getter
public enum EFilter {
    GT(">"),
    GE(">="),
    LT("<"),
    LE("<="),
    EQ("="),
    LIKE("like"),
    IN("in"),
    NOTIN("not in"),
    NE("<>", "!=", "not"),
    NOTNULL("not null", "is not null"),
    NULL("null", "is null");

    private List<String> val;

    EFilter(String ...val) {
        this.val = Arrays.asList(val);
    }

    @JsonCreator
    public static EFilter fromText(String filter) {
        if (filter == null) {
            return null;
        }
        var filterLower = filter.toLowerCase();
        return Arrays.stream(EFilter.values())
                .filter(val -> val.name().equalsIgnoreCase(filterLower) || val.getVal().contains(filterLower))
                .findFirst().orElse(null);
    }

}
