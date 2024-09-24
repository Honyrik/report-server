package ru.tehnobear.essence.receiver.dto.admin.dictionary;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.lang.NonNull;
import ru.tehnobear.essence.dao.entries.TDAssetType;

import java.util.HashMap;

@Schema(implementation = DAssetTypeUpdate.DAssetTypePatchData.class)
@JsonIgnoreProperties({"ckUser","ctChange","ctCreate"})
public class DAssetTypeUpdate extends HashMap<String, Object> {

    @JsonIgnoreProperties({"ckUser","ctChange","ctCreate"})
    public static class DAssetTypePatchData extends TDAssetType {
        @NonNull
        public String ckId;
        public String ckIdOld;
    }
}
