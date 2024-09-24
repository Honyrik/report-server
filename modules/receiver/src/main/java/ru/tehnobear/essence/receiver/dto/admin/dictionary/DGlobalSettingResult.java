package ru.tehnobear.essence.receiver.dto.admin.dictionary;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import org.springframework.lang.NonNull;
import ru.tehnobear.essence.dao.entries.TDGlobalSetting;
import ru.tehnobear.essence.share.dto.Result;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class DGlobalSettingResult extends Result {
    @NonNull
    private List<TDGlobalSetting> data;
}
