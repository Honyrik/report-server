package ru.tehnobear.essence.receiver.dto.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.NonNull;
import ru.tehnobear.essence.dao.entries.TAsset;
import ru.tehnobear.essence.dao.entries.TDAssetType;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"ckId","cbAsset","ckUser","ctChange","ctCreate","clDeleted"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssetInsert extends TAsset {
    @NonNull
    private String cvName;
    @NonNull
    private DAssetType type;

    @JsonIgnoreProperties({
            "cvDescription",
            "crType",
            "cvContentType",
            "cvExtension",
            "ckUser",
            "ctChange",
            "ctCreate",
            "clDeleted"
    })
    public static class DAssetType extends TDAssetType {
        @NonNull
        public String ckId;
    }
}
