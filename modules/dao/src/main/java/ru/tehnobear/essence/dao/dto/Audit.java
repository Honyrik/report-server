package ru.tehnobear.essence.dao.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.NumericBooleanConverter;
import ru.tehnobear.essence.dao.util.Util;

import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Getter
@Setter
@EqualsAndHashCode
@RequiredArgsConstructor
@SuperBuilder
@MappedSuperclass
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
@SQLRestriction("not clDeleted")
public abstract class Audit {
    @Column(name="\"ck_user\"", nullable = false)
    private String ckUser;

    @Column(name="\"ct_change\"", nullable = false)
    @UpdateTimestamp
    private Instant ctChange;

    @Column(name="\"ct_create\"", updatable = false, nullable = false)
    @CreationTimestamp
    private Instant ctCreate;

    @Column(name="\"cl_deleted\"", nullable = false)
    @Builder.Default
    @Convert(converter = NumericBooleanConverter.class)
    private boolean clDeleted = false;

    @PrePersist
    protected void onCreate() {
        ctCreate = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        ctChange = Instant.now();
    }

    public String toStringPostgres(String value) {
        return value == null ? "NULL" : "'" + value.replaceAll("'", "''") + "'";
    }

    public String toStringPostgres(UUID value) {
        return value == null ? "NULL" : toStringPostgres(value.toString());
    }

    public String toStringPostgres(byte[] value) {
        return value == null ? "NULL" : "DECODE('" + Base64.getEncoder().encodeToString(value) + "', 'BASE64')";
    }

    public String toStringPostgres(Instant value) {
        return toStringPostgres(value.toString());
    }

    public String toStringPostgres(Map<String, Object> value) {
        try {
            return toStringPostgres(Util.objectMapperAll.writeValueAsString(value));
        } catch (JsonProcessingException e) {
            log.debug(e.getLocalizedMessage(), e);
            return toStringPostgres("{}");
        }
    }

    public String toStringPostgres(Boolean value) {
        return value == null || !value ? "0" : "1";
    }

    public String toStringPostgres(Number value) {
        return String.format("%d", value);
    }

    public String toStringPostgres(boolean value) {
        return value ? "1" : "0";
    }

    public String toStringPostgres(Enum value) {
        return value == null ? "NULL" : "'" + value.name() + "'";
    }

    public abstract String toPostgresPatch();
}
