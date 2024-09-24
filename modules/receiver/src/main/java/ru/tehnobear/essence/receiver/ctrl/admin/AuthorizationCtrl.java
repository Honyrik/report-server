package ru.tehnobear.essence.receiver.ctrl.admin;

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
import ru.tehnobear.essence.receiver.api.admin.AdminApi;
import ru.tehnobear.essence.receiver.dto.admin.*;
import ru.tehnobear.essence.receiver.service.admin.AuthorizationService;
import ru.tehnobear.essence.receiver.service.admin.UserService;
import ru.tehnobear.essence.share.dto.Result;
import ru.tehnobear.essence.share.service.AuditService;
import ru.tehnobear.essence.share.util.QueryUtil;
import ru.tehnobear.essence.util.Roles;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Authorization", description = "Управление Authorization")
public class AuthorizationCtrl implements AdminApi {
    private final AuthorizationService service;
    private final UserService userService;
    private final AuditService auditService;
    @Operation(summary = "Fetch Authorization")
    @PostMapping(value = "/Authorization", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = Void.class)))
    public Mono<AuthorizationResult> fetchAuthorization(@RequestBody AuthorizationFetch fetch) {
        return service.fetch(fetch);
    }
    @Operation(summary = "Insert Authorization")
    @PutMapping(value = "/Authorization", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = Void.class)))
    @PreAuthorize(Roles.ROLE_ADMIN_AUTHORITY)
    public Mono<Result> insert(
            @RequestBody AuthorizationInsert data
    ) {
        return userService
                .getUser()
                .flatMap(user -> {
                    var tLog = TLog.builder()
                            .ccJson(QueryUtil.util.writeValueAsStringSilentAll(data))
                            .ckUser(user)
                            .cvAction("PUT")
                            .cvTable("Authorization");
                    return Mono.defer(() -> service.insert(data, user))
                            .doOnSuccess(val -> auditService.save(tLog.cvId(Optional.ofNullable(val.getCkId()).orElse("").toString()).build()))
                            .doOnError(err -> auditService.saveError(tLog.build(), err));
                });
    }
    @Operation(summary = "Update Authorization")
    @PatchMapping(value = "/Authorization", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = Void.class)))
    @PreAuthorize(Roles.ROLE_ADMIN_AUTHORITY)
    public Mono<Result> update(
            @RequestBody AuthorizationUpdate data
    ) {
        return userService
                .getUser()
                .flatMap(user -> {
                    var tLog = TLog.builder()
                            .ccJson(QueryUtil.util.writeValueAsStringSilentAll(data))
                            .ckUser(user)
                            .cvAction("PATCH")
                            .cvTable("Authorization");
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
    @Operation(summary = "Delete Authorization")
    @DeleteMapping(value = "/Authorization", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = Void.class)))
    @PreAuthorize(Roles.ROLE_ADMIN_AUTHORITY)
    public Mono<Result> delete(@RequestBody AuthorizationDelete data) {
        return userService
                .getUser()
                .flatMap(user -> {
                    var tLog = TLog.builder()
                            .ccJson(QueryUtil.util.writeValueAsStringSilentAll(data))
                            .ckUser(user)
                            .cvId(data.getCkId().toString())
                            .cvAction("DELETE")
                            .cvTable("Authorization");
                    return Mono.defer(() -> service.delete(data, user))
                            .doOnSuccess(val -> auditService.save(tLog.build()))
                            .doOnError(err -> auditService.saveError(tLog.build(), err));
                });
    }
}
