package ru.tehnobear.essence.share.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public abstract class Fetch {
    private List<Filter> filter;
    private List<Sort> sort;
    private Long fetch;
    private Long offset;
    public abstract Map<String, Object> getData();
}
