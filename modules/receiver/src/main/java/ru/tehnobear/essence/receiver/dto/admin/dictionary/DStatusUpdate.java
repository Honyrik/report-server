package ru.tehnobear.essence.receiver.dto.admin.dictionary;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.lang.NonNull;
import ru.tehnobear.essence.dao.entries.TDStatus;

import java.util.HashMap;

@Schema(implementation = DStatusUpdate.DStatusPatchData.class)
@JsonIgnoreProperties({"ckUser","ctChange","ctCreate"})
public class DStatusUpdate extends HashMap<String, Object> {

    @JsonIgnoreProperties({"ckUser","ctChange","ctCreate"})
    public static class DStatusPatchData extends TDStatus {
        @NonNull
        public String ckId;
        public String ckIdOld;
    }
}
