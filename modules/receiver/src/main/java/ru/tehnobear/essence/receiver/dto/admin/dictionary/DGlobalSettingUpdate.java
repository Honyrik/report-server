package ru.tehnobear.essence.receiver.dto.admin.dictionary;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.lang.NonNull;
import ru.tehnobear.essence.dao.entries.TDGlobalSetting;

import java.util.HashMap;

@Schema(implementation = DGlobalSettingUpdate.DGlobalSettingPatchData.class)
@JsonIgnoreProperties({"ckUser","ctChange","ctCreate"})
public class DGlobalSettingUpdate extends HashMap<String, Object> {

    @JsonIgnoreProperties({"ckUser","ctChange","ctCreate"})
    public static class DGlobalSettingPatchData extends TDGlobalSetting {
        @NonNull
        public String ckId;
        public String ckIdOld;
    }
}
