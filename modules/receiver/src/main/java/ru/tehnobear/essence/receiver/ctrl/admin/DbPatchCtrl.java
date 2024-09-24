package ru.tehnobear.essence.receiver.ctrl.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.tehnobear.essence.dao.entries.TLog;
import ru.tehnobear.essence.receiver.api.admin.AdminApi;
import ru.tehnobear.essence.receiver.dto.admin.CreatePatchFetch;
import ru.tehnobear.essence.receiver.dto.admin.CreatePatchResult;
import ru.tehnobear.essence.receiver.dto.admin.DbPatchBody;
import ru.tehnobear.essence.receiver.service.admin.DbPatchService;
import ru.tehnobear.essence.receiver.service.admin.UserService;
import ru.tehnobear.essence.share.dto.Result;
import ru.tehnobear.essence.share.service.AuditService;
import ru.tehnobear.essence.share.util.QueryUtil;
import ru.tehnobear.essence.util.Roles;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin DB Patch", description = "Создание патча liquibase")
public class DbPatchCtrl implements AdminApi {
    private final DbPatchService service;
    private final UserService userService;
    private final AuditService auditService;
    @Operation(summary = "Fetch Asset")
    @PostMapping(value = "/DbPatch", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = Void.class)))
    public Mono<CreatePatchResult> fetch(@RequestBody CreatePatchFetch fetch) {
        return service.fetch(fetch);
    }
    @Operation(summary = "File DB Patch")
    @GetMapping(value = "/getFileDbPatch/{id}")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = Void.class)))
    @PreAuthorize(Roles.ROLE_ADMIN_AUTHORITY)
    Mono<ResponseEntity<Object>> generateDbPatch(@PathVariable UUID id) {
        return Mono.defer(() -> service.getFileDbPatch(id));
    }
    @Operation(summary = "Generate DB Patch")
    @PostMapping(value = "/generateDbPatch", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = Void.class)))
    @PreAuthorize(Roles.ROLE_ADMIN_AUTHORITY)
    Mono<ResponseEntity<Object>> generateDbPatch(@RequestBody DbPatchBody data) {
        return userService
                .getUser()
                .flatMap(user -> {
                    var tLog = TLog.builder()
                            .ccJson(QueryUtil.util.writeValueAsStringSilentAll(data))
                            .ckUser(user)
                            .cvAction("POST")
                            .cvTable("generateDbPatch");
                    return Mono.defer(() -> {
                                try {
                                    return service.generateDbPatch(data, user);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            })
                            .doOnSuccess(val -> auditService.save(tLog.cvId("").build()))
                            .doOnError(err -> auditService.saveError(tLog.build(), err));
                });
    }
    @Operation(summary = "Delete DB Patch")
    @DeleteMapping(value = "/DbPatch/{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = Void.class)))
    @PreAuthorize(Roles.ROLE_ADMIN_AUTHORITY)
    public Mono<Result> delete(@PathVariable UUID id) {
        return userService
                .getUser()
                .flatMap(user -> {
                    var tLog = TLog.builder()
                            .ccJson(QueryUtil.util.writeValueAsStringSilentAll(Map.of("ckId", id)))
                            .ckUser(user)
                            .cvId(id.toString())
                            .cvAction("DELETE")
                            .cvTable("DbPatch");
                    return Mono.defer(() -> service.delete(id, user))
                            .doOnSuccess(val -> auditService.save(tLog.build()))
                            .doOnError(err -> auditService.saveError(tLog.build(), err));
                });
    }
}
