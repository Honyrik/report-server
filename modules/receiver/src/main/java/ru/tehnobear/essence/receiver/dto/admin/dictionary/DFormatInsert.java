package ru.tehnobear.essence.receiver.dto.admin.dictionary;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.NonNull;
import ru.tehnobear.essence.dao.dto.EDFormatType;
import ru.tehnobear.essence.dao.entries.TDFormat;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"ckUser","ctChange","ctCreate","clDeleted"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DFormatInsert extends TDFormat {
    @NonNull
    private String ckId;
    @NonNull
    private String cvContentType;
    @NonNull
    private String cvExtension;
    @NonNull
    private String cvName;
    @NonNull
    private EDFormatType crType;
}
