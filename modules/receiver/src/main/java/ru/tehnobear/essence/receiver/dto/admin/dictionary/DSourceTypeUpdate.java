package ru.tehnobear.essence.receiver.dto.admin.dictionary;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.lang.NonNull;
import ru.tehnobear.essence.dao.entries.TDSourceType;

import java.util.HashMap;

@Schema(implementation = DSourceTypeUpdate.DSourceTypePatchData.class)
@JsonIgnoreProperties({"ckUser","ctChange","ctCreate"})
public class DSourceTypeUpdate extends HashMap<String, Object> {

    @JsonIgnoreProperties({"ckUser","ctChange","ctCreate"})
    public static class DSourceTypePatchData extends TDSourceType {
        @NonNull
        public String ckId;
        public String ckIdOld;
    }
}
