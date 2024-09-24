package ru.tehnobear.essence.receiver.dto.admin.dictionary;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.lang.NonNull;
import ru.tehnobear.essence.dao.entries.TDFormat;

import java.util.HashMap;

@Schema(implementation = DFormatUpdate.DFormatPatchData.class)
@JsonIgnoreProperties({"ckUser","ctChange","ctCreate"})
public class DFormatUpdate extends HashMap<String, Object> {

    @JsonIgnoreProperties({"ckUser","ctChange","ctCreate"})
    public static class DFormatPatchData extends TDFormat {
        @NonNull
        public String ckId;
        public String ckIdOld;
    }
}
