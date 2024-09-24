package ru.tehnobear.essence.share.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.lang.NonNull;

@Data
@Builder
public class FileStore {
    @NonNull
    private byte[] file;
    @NonNull
    private String name;
    @NonNull
    private String contentType;
}
