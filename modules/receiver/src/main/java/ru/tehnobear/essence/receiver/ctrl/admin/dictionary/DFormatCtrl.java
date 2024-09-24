package ru.tehnobear.essence.receiver.ctrl.admin.dictionary;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.tehnobear.essence.dao.entries.TLog;
import ru.tehnobear.essence.receiver.api.admin.DictionaryApi;
import ru.tehnobear.essence.receiver.dto.admin.dictionary.*;
import ru.tehnobear.essence.receiver.service.admin.UserService;
import ru.tehnobear.essence.receiver.service.admin.dictionary.DFormatService;
import ru.tehnobear.essence.share.dto.Result;
import ru.tehnobear.essence.share.service.AuditService;
import ru.tehnobear.essence.share.util.QueryUtil;
import ru.tehnobear.essence.util.Roles;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Dictionary Admin DFormat", description = "Справочник форматов печати")
public class DFormatCtrl implements DictionaryApi {
    private final DFormatService service;
    private final UserService userService;
    private final AuditService auditService;
    @Operation(summary = "Fetch DFormat")
    @PostMapping(value = "/DFormat", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = Void.class)))
    public Mono<DFormatResult> fetchDFormat(@RequestBody DFormatFetch fetch) {
        return service.fetch(fetch);
    }
    @Operation(summary = "Insert DFormat")
    @PutMapping(value = "/DFormat", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = Void.class)))
    @PreAuthorize(Roles.ROLE_ADMIN_AUTHORITY)
    public Mono<Result> insert(
            @RequestBody DFormatInsert data
    ) {
        return userService
                .getUser()
                .flatMap(user -> {
                    var tLog = TLog.builder()
                            .ccJson(QueryUtil.util.writeValueAsStringSilentAll(data))
                            .ckUser(user)
                            .cvAction("PUT")
                            .cvTable("DFormat");
                    return Mono.defer(() -> service.insert(data, user))
                            .doOnSuccess(val -> auditService.save(tLog.cvId(Optional.ofNullable(val.getCkId()).orElse("").toString()).build()))
                            .doOnError(err -> auditService.saveError(tLog.build(), err));
                });
    }
    @Operation(summary = "Update DFormat")
    @PatchMapping(value = "/DFormat", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = Void.class)))
    @PreAuthorize(Roles.ROLE_ADMIN_AUTHORITY)
    public Mono<Result> update(
            @RequestBody DFormatUpdate data
    ) {
        return userService
                .getUser()
                .flatMap(user -> {
                    var tLog = TLog.builder()
                            .ccJson(QueryUtil.util.writeValueAsStringSilentAll(data))
                            .ckUser(user)
                            .cvAction("PATCH")
                            .cvTable("DFormat");
                    return Mono.defer(() -> service.update(data, user))
                            .doOnSuccess(val -> auditService.save(tLog.cvId(Optional.ofNullable(val.getCkId()).orElse("").toString()).build()))
                            .doOnError(err -> auditService.saveError(
                                    tLog.cvId(
                                            Optional.ofNullable(data.get("ckIdOld"))
                                                    .orElse(Optional.ofNullable(data.get("ckId")).orElse(""))
                                                    .toString()
                                    ).build(),
                                    err
                            ));
                });
    }
    @Operation(summary = "Delete DFormat")
    @DeleteMapping(value = "/DFormat", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = Void.class)))
    @PreAuthorize(Roles.ROLE_ADMIN_AUTHORITY)
    public Mono<Result> delete(@RequestBody DFormatDelete data) {
        return userService
                .getUser()
                .flatMap(user -> {
                    var tLog = TLog.builder()
                            .ccJson(QueryUtil.util.writeValueAsStringSilentAll(data))
                            .ckUser(user)
                            .cvId(data.getCkId().toString())
                            .cvAction("DELETE")
                            .cvTable("DFormat");
                    return Mono.defer(() -> service.delete(data, user))
                            .doOnSuccess(val -> auditService.save(tLog.build()))
                            .doOnError(err -> auditService.saveError(tLog.build(), err));
                });
    }
}
