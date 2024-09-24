package ru.tehnobear.essence.receiver.dto.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.lang.NonNull;
import ru.tehnobear.essence.dao.entries.TAuthorization;

import java.util.HashMap;
import java.util.UUID;

@Schema(implementation = AuthorizationUpdate.AuthorizationPatchData.class)
@JsonIgnoreProperties({"ckUser","ctChange","ctCreate"})
public class AuthorizationUpdate extends HashMap<String, Object> {

    @JsonIgnoreProperties({"ckUser","ctChange","ctCreate"})
    public static class AuthorizationPatchData extends TAuthorization {
        @NonNull
        public UUID ckId;
        public UUID ckIdOld;
    }
}
