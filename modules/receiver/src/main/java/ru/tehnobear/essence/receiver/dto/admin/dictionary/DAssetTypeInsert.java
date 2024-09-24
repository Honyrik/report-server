package ru.tehnobear.essence.receiver.dto.admin.dictionary;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.NonNull;
import ru.tehnobear.essence.dao.dto.EAssetType;
import ru.tehnobear.essence.dao.entries.TDAssetType;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"ckUser","ctChange","ctCreate","clDeleted"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DAssetTypeInsert extends TDAssetType {
    @NonNull
    private String ckId;
    @NonNull
    private EAssetType crType;
}
