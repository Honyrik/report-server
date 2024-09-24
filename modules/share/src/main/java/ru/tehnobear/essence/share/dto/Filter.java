package ru.tehnobear.essence.share.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.lang.NonNull;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class Filter {
    @NonNull
    private EFilter operator;
    @NonNull
    private String property;
    private String format;
    private String datatype;
    @EqualsAndHashCode.Exclude
    @Schema(oneOf = {String.class, Number.class, Boolean.class, Instant.class, UUID.class, ArrayValue.class})
    private Object value;

    @ArraySchema(schema = @Schema(type = "array", oneOf = {String.class, Number.class, Instant.class, UUID.class, Boolean.class}))
    private interface ArrayValue {}
}
