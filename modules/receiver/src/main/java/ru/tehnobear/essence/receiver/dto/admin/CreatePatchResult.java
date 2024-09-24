package ru.tehnobear.essence.receiver.dto.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import org.springframework.lang.NonNull;
import ru.tehnobear.essence.dao.entries.TCreatePatch;
import ru.tehnobear.essence.share.dto.Result;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class CreatePatchResult extends Result {
    @NonNull
    @JsonIgnoreProperties({"cbAsset"})
    private List<TCreatePatch> data;
}
