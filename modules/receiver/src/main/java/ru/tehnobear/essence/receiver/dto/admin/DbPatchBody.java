package ru.tehnobear.essence.receiver.dto.admin;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class DbPatchBody {
    @NonNull
    private List<UUID> reports;
}
