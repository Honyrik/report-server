package ru.tehnobear.essence.share.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.lang.NonNull;

import java.util.UUID;

@Data
@Builder
public class ReportBody {
    @NonNull
    private UUID ckId;
    @NonNull
    private String sing;
}
