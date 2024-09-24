package ru.tehnobear.essence.receiver.dto.admin.dictionary;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.lang.NonNull;
import ru.tehnobear.essence.dao.entries.TDQueue;

import java.util.HashMap;

@Schema(implementation = DQueueUpdate.DQueuePatchData.class)
@JsonIgnoreProperties({"ckUser","ctChange","ctCreate"})
public class DQueueUpdate extends HashMap<String, Object> {

    @JsonIgnoreProperties({"ckUser","ctChange","ctCreate"})
    public static class DQueuePatchData extends TDQueue {
        @NonNull
        public String ckId;
        public String ckIdOld;
        public DQueueInsert.DQueue parent;
    }
}
