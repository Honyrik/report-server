package ru.tehnobear.essence.receiver.dto.admin;

import lombok.Data;
import org.springframework.lang.NonNull;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
public class PublicReportBody {
    @NonNull
    private UUID reportId;
    @NonNull
    private String format;
    private String user;
    private Map<String, Object> parameter = new HashMap<>();
    private String reportName;
    private Instant cleanDate;
    private boolean isOnline = false;
}
