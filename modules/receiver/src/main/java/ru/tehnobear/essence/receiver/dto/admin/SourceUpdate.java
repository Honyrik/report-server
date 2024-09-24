package ru.tehnobear.essence.receiver.dto.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.lang.NonNull;
import ru.tehnobear.essence.dao.entries.TSource;

import java.util.HashMap;

@Schema(implementation = SourceUpdate.SourcePatchData.class)
@JsonIgnoreProperties({"ckUser","ctChange","ctCreate"})
public class SourceUpdate extends HashMap<String, Object> {

    @JsonIgnoreProperties({"ckUser","ctChange","ctCreate"})
    public static class SourcePatchData extends TSource {
        @NonNull
        public String ckId;
        public String ckIdOld;
        public SourceInsert.DSourceType sourceType;
    }
}
