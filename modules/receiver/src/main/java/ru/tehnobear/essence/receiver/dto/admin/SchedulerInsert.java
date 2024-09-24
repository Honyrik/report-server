package ru.tehnobear.essence.receiver.dto.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.NonNull;
import ru.tehnobear.essence.dao.entries.TReport;
import ru.tehnobear.essence.dao.entries.TReportFormat;
import ru.tehnobear.essence.dao.entries.TScheduler;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"ckId","ckUser","ctChange","ctCreate","clDeleted"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SchedulerInsert extends TScheduler {
    @NonNull
    private String cvUnixCron;
    @NonNull
    private Report report;
    @NonNull
    private Format format;
    @JsonIgnoreProperties({
            "cvDurationExpireStorageOnline",
            "cvDurationExpireStorageOffline",
            "cctParameter",
            "cvName",
            "cnPriority",
            "ckUser",
            "ctChange",
            "ctCreate",
            "clDeleted"
    })
    public static class Report extends TReport {
        @NonNull
        public UUID ckId;
    }

    @JsonIgnoreProperties({
            "report",
            "format",
            "source",
            "asset",
            "ckUser",
            "ctChange",
            "ctCreate",
            "clDeleted"
    })
    public static class Format extends TReportFormat {
        @NonNull
        public String ckId;
    }

}
