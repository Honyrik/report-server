package ru.tehnobear.essence.receiver.dto.admin.dictionary;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.NonNull;

@Getter
@Setter
@NoArgsConstructor
public class DGlobalSettingDelete {
    @NonNull
    private String ckId;
}
