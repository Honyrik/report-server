package ru.tehnobear.essence.receiver.dto.admin.dictionary;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.NonNull;
import ru.tehnobear.essence.dao.dto.EStatus;
import ru.tehnobear.essence.dao.entries.TDStatus;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"ckUser","ctChange","ctCreate","clDeleted"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DStatusInsert extends TDStatus {
    @NonNull
    private EStatus ckId;
}
