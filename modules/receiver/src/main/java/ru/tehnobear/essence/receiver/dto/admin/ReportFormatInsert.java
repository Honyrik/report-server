package ru.tehnobear.essence.receiver.dto.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.NonNull;
import ru.tehnobear.essence.dao.entries.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"ckId","ckUser","ctChange","ctCreate","clDeleted"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportFormatInsert extends TReportFormat {
    @NonNull
    private Asset asset;
    @NonNull
    private Report report;
    @NonNull
    private Format format;
    @NonNull
    private Source source;

    @JsonIgnoreProperties({"sourceType","cvPlugin","cctParameter","clEnable","ckUser","ctChange","ctCreate","clDeleted"})
    public static class Source extends TSource {
        @NonNull
        public UUID ckId;
    }

    @JsonIgnoreProperties({"cbAsset","cvAsset","cctParameter","cvName","type","ckUser","ctChange","ctCreate","clDeleted"})
    public static class Asset extends TAsset {
        @NonNull
        public UUID ckId;
    }

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
            "cvName",
            "cctParameter",
            "cvContentType",
            "cvExtension",
            "crType",
            "cvPlugin",
            "ckUser",
            "ctChange",
            "ctCreate",
            "clDeleted"
    })
    public static class Format extends TDFormat {
        @NonNull
        public String ckId;
    }
}
