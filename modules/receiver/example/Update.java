package ru.tehnobear.essence.receiver.dto.admin.dictionary.example;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.lang.NonNull;
import ru.tehnobear.essence.dao.entries.TExample;

import java.util.HashMap;
import java.util.UUID;

@Schema(implementation = ExampleUpdate.ExamplePatchData.class)
@JsonIgnoreProperties({"ckUser","ctChange","ctCreate"})
public class ExampleUpdate extends HashMap<String, Object> {

    @JsonIgnoreProperties({"ckUser","ctChange","ctCreate"})
    public static class ExamplePatchData extends TExample {
        @NonNull
        public UUID ckId;
        public UUID ckIdOld;
    }
}
