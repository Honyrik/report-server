package ru.tehnobear.essence.receiver.dto.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.lang.NonNull;
import ru.tehnobear.essence.dao.entries.TQueue;

import java.util.HashMap;
import java.util.UUID;

@Schema(implementation = QueueUpdate.QueuePatchData.class)
@JsonIgnoreProperties({"ckUser","ctChange","ctCreate"})
public class QueueUpdate extends HashMap<String, Object> {

    @JsonIgnoreProperties({"ckUser","ctChange","ctCreate"})
    public static class QueuePatchData extends TQueue {
        @NonNull
        public UUID ckId;
        public UUID ckIdOld;
        private QueueInsert.Report report;
        private QueueInsert.Format format;
        private QueueInsert.Queue queue;
        private QueueInsert.Status status;
        private QueueInsert.Scheduler scheduler;
    }
}
