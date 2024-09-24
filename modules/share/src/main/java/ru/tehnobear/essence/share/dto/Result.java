package ru.tehnobear.essence.share.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.lang.NonNull;

import java.util.*;

@Data
@Setter
@SuperBuilder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result {
    @NonNull
    @Builder.Default
    private boolean success = true;
    @Schema(oneOf = {String.class, Number.class, UUID.class, Boolean.class})
    private Object ckId;
    private Long total;

    @Schema(implementation = MessageMap.class)
    private Map<EMessage, List<List<String>>> message;
    @JsonIgnore
    @Builder.Default
    private boolean isError = false;
    @JsonIgnore
    @Builder.Default
    private boolean isWarning = false;

    public Result addMessage(EMessage eMessage, String ...msg) {
        if (Arrays.stream(msg).filter(val -> val != null && !val.trim().isEmpty()).findFirst().isEmpty()) {
            return this;
        }
        if (message == null) {
            message = new HashMap<>();
        }
        if (!message.containsKey(eMessage)) {
            message.put(eMessage, new ArrayList<>());
        }
        if (eMessage == EMessage.ERROR) {
            success = false;
            isError = true;
        }
        if (eMessage == EMessage.WARNING) {
            isWarning = true;
        }
        var msgArr = message.get(eMessage);
        msgArr.add(Arrays.asList(msg));
        return this;
    }

    public Result addError(String ...msg) {
        return addMessage(EMessage.ERROR, msg);
    }

    public Result addWarning(String ...msg) {
        return addMessage(EMessage.WARNING, msg);
    }

    private static class MessageMap {
        public List<List<String>> all;
        public List<List<String>> warning;
        public List<List<String>> error;
        public List<List<String>> info;
        public List<List<String>> notification;
        public List<List<String>> debug;
        public List<List<String>> block;
        public List<List<String>> unblock;
    }
}
