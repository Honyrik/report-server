package ru.tehnobear.essence.receiver.dto.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;
import ru.tehnobear.essence.dao.entries.TAsset;
import ru.tehnobear.essence.share.dto.Fetch;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@AllArgsConstructor
@RequiredArgsConstructor
public class AssetFetch extends Fetch {
    @Schema(implementation = TAssetData.class)
    private Map<String, Object> data;
    @Override
    public Map<String, Object> getData() {
        return this.data;
    }

    @JsonIgnoreProperties({"cbAsset","cvAsset","cctParameter"})
    private static class TAssetData extends TAsset{}
}
