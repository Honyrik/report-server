package ru.tehnobear.essence.receiver.dto.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.lang.NonNull;
import ru.tehnobear.essence.dao.entries.TAsset;

import java.util.HashMap;
import java.util.UUID;

@Schema(implementation = AssetUpdate.AssetPatchData.class)
@JsonIgnoreProperties({"cbAsset","ckUser","ctChange","ctCreate"})
public class AssetUpdate extends HashMap<String, Object> {

    @JsonIgnoreProperties({"cbAsset","ckUser","ctChange","ctCreate"})
    public static class AssetPatchData extends TAsset {
        @NonNull
        public UUID ckId;
        public UUID ckIdOld;
        public AssetInsert.DAssetType type;
    }
}
