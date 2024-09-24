package ru.tehnobear.essence.receiver.dto.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.lang.NonNull;
import ru.tehnobear.essence.dao.entries.TScheduler;

import java.util.HashMap;
import java.util.UUID;

@Schema(implementation = SchedulerUpdate.SchedulerPatchData.class)
@JsonIgnoreProperties({"ckUser","ctChange","ctCreate"})
public class SchedulerUpdate extends HashMap<String, Object> {

    @JsonIgnoreProperties({"ckUser","ctChange","ctCreate"})
    public static class SchedulerPatchData extends TScheduler {
        @NonNull
        public UUID ckId;
        public UUID ckIdOld;
        public SchedulerInsert.Report report;
        public SchedulerInsert.Format format;
    }
}
