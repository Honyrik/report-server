package ru.tehnobear.essence.receiver.dto.admin.dictionary;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.NonNull;
import ru.tehnobear.essence.dao.entries.TDQueue;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"ckUser","ctChange","ctCreate","clDeleted"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DQueueInsert extends TDQueue {
    @NonNull
    private String ckId;
    private String ckParent;

    private DQueue parent;

    @JsonIgnoreProperties({"cvRunnerUrl","parent","ckUser","ctChange","ctCreate","clDeleted"})
    public static class DQueue extends TDQueue {
        @NonNull
        private String ckId;
    }
}
