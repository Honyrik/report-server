package ru.tehnobear.essence.receiver.dto.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.lang.NonNull;
import ru.tehnobear.essence.dao.entries.TReportFormat;

import java.util.HashMap;
import java.util.UUID;

@Schema(implementation = ReportFormatUpdate.ReportFormatPatchData.class)
@JsonIgnoreProperties({"ckUser","ctChange","ctCreate"})
public class ReportFormatUpdate extends HashMap<String, Object> {

    @JsonIgnoreProperties({"ckUser","ctChange","ctCreate"})
    public static class ReportFormatPatchData extends TReportFormat {
        @NonNull
        public UUID ckId;
        public UUID ckIdOld;
        public ReportFormatInsert.Asset asset;
        public ReportFormatInsert.Report report;
        public ReportFormatInsert.Format format;
        public ReportFormatInsert.Source source;
    }
}
