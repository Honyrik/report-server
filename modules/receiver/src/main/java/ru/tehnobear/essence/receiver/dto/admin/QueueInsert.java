package ru.tehnobear.essence.receiver.dto.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.NonNull;
import ru.tehnobear.essence.dao.entries.*;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"ckId", "server","ckUser","ctChange","ctCreate","clDeleted"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QueueInsert extends TQueue {
    @NonNull
    private Map<String, Object> cctParameter;
    @NonNull
    private Report report;
    @NonNull
    private Format format;
    @NonNull
    private Queue queue;
    @NonNull
    private Status status;
    private Scheduler scheduler;

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
    @JsonIgnoreProperties({
            "parent",
            "cvRunnerUrl",
            "ckUser",
            "ctChange",
            "ctCreate",
            "clDeleted"
    })
    public static class Queue extends TDQueue {
        @NonNull
        public UUID ckId;
    }

    @JsonIgnoreProperties({
            "cvName",
            "ckUser",
            "ctChange",
            "ctCreate",
            "clDeleted"
    })
    public static class Status extends TDStatus {
        @NonNull
        public String ckId;
    }

    @JsonIgnoreProperties({
            "report",
            "format",
            "cvUnixCron",
            "cvReportName",
            "ctStartRunCron",
            "ctNextRunCron",
            "cnPriority",
            "clEnable",
            "ckUser",
            "ctChange",
            "ctCreate",
            "clDeleted"
    })
    public static class Scheduler extends TScheduler {
        @NonNull
        public String ckId;
    }
}
