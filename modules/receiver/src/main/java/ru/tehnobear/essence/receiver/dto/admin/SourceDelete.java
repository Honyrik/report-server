package ru.tehnobear.essence.receiver.dto.admin;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.NonNull;

@Getter
@Setter
@NoArgsConstructor
public class SourceDelete {
    @NonNull
    private String ckId;
}
