package ru.tehnobear.essence.receiver.dto.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.NonNull;
import ru.tehnobear.essence.dao.entries.TAuthorization;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"ckId","ckUser","ctChange","ctCreate","clDeleted"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthorizationInsert extends TAuthorization {
    @NonNull
    private String cvName;
    @NonNull
    private String cvPlugin;
}
