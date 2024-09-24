package ru.tehnobear.essence.share.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.lang.NonNull;

@Data
@Builder
public class Dictionary {
    @NonNull
    private String id;
    private String name;
    private String description;
}
