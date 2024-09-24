package ru.tehnobear.essence.share.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.lang.NonNull;

@Data
@AllArgsConstructor
public class Sort {
    @NonNull
    private ESort direction;
    @NonNull
    private String property;
}
