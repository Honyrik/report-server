package ru.tehnobear.essence.receiver.dto.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.lang.NonNull;
import ru.tehnobear.essence.dao.entries.TReport;

import java.util.HashMap;
import java.util.UUID;

@Schema(implementation = ReportUpdate.ReportPatchData.class)
@JsonIgnoreProperties({"ckUser","ctChange","ctCreate"})
public class ReportUpdate extends HashMap<String, Object> {

    @JsonIgnoreProperties({"ckUser","ctChange","ctCreate"})
    public static class ReportPatchData extends TReport {
        @NonNull
        public UUID ckId;
        public UUID ckIdOld;
    }
}
