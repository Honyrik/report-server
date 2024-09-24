package ru.tehnobear.essence.receiver.dto.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.lang.NonNull;
import ru.tehnobear.essence.dao.entries.TReportAsset;

import java.util.HashMap;
import java.util.UUID;

@Schema(implementation = ReportAssetUpdate.ReportAssetPatchData.class)
@JsonIgnoreProperties({"ckUser","ctChange","ctCreate"})
public class ReportAssetUpdate extends HashMap<String, Object> {

    @JsonIgnoreProperties({"ckUser","ctChange","ctCreate"})
    public static class ReportAssetPatchData extends TReportAsset {
        @NonNull
        public UUID ckId;
        public UUID ckIdOld;
        public ReportAssetInsert.Asset asset;
        public ReportAssetInsert.Report report;
        public ReportAssetInsert.Format format;
    }
}
