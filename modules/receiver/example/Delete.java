package ru.tehnobear.essence.receiver.dto.admin.dictionary.example;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.NonNull;
import ru.tehnobear.essence.dao.entries.TExample;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class ExampleDelete {
    @NonNull
    private UUID ckId;
}
