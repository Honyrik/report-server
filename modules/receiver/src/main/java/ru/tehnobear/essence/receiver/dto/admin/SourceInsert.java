package ru.tehnobear.essence.receiver.dto.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.NonNull;
import ru.tehnobear.essence.dao.entries.TDSourceType;
import ru.tehnobear.essence.dao.entries.TSource;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"ckUser","ctChange","ctCreate","clDeleted"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SourceInsert extends TSource {
    @NonNull
    private String ckId;

    @NonNull
    private DSourceType sourceType;

    @JsonIgnoreProperties({"cvDescription","ckUser","ctChange","ctCreate","clDeleted"})
    public static class DSourceType extends TDSourceType {
        @NonNull
        public String ckId;
    }
}
