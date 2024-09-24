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
import ru.tehnobear.essence.receiver.service.admin.dictionary.DQueueService;
import ru.tehnobear.essence.share.dto.Result;
import ru.tehnobear.essence.share.service.AuditService;
import ru.tehnobear.essence.share.util.QueryUtil;
import ru.tehnobear.essence.util.Roles;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Dictionary Admin DQueue", description = "Справочник групп обработчиков")
public class DQueueCtrl implements DictionaryApi {
    private final DQueueService service;
    private final UserService userService;
    private final AuditService auditService;
    @Operation(summary = "Fetch DQueue")
    @PostMapping(value = "/DQueue", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = Void.class)))
    public Mono<DQueueResult> fetchDQueue(@RequestBody DQueueFetch fetch) {
        return service.fetch(fetch);
    }
    @Operation(summary = "Fetch DQueue")
    @PostMapping(value = "/DQueue/treeList", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = Void.class)))
    public Mono<DQueueListResult> fetchDQueueTreeList(@RequestBody DQueueFetch fetch) {
        return service.fetchTreeList(fetch);
    }
    @Operation(summary = "Insert DQueue")
    @PutMapping(value = "/DQueue", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = Void.class)))
    @PreAuthorize(Roles.ROLE_ADMIN_AUTHORITY)
    public Mono<Result> insert(
            @RequestBody DQueueInsert data
    ) {
        return userService
                .getUser()
                .flatMap(user -> {
                    var tLog = TLog.builder()
                            .ccJson(QueryUtil.util.writeValueAsStringSilentAll(data))
                            .ckUser(user)
                            .cvAction("PUT")
                            .cvTable("DQueue");
                    return Mono.defer(() -> service.insert(data, user))
                            .doOnSuccess(val -> auditService.save(tLog.cvId(Optional.ofNullable(val.getCkId()).orElse("").toString()).build()))
                            .doOnError(err -> auditService.saveError(tLog.build(), err));
                });
    }
    @Operation(summary = "Update DQueue")
    @PatchMapping(value = "/DQueue", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = Void.class)))
    @PreAuthorize(Roles.ROLE_ADMIN_AUTHORITY)
    public Mono<Result> update(
            @RequestBody DQueueUpdate data
    ) {
        return userService
                .getUser()
                .flatMap(user -> {
                    var tLog = TLog.builder()
                            .ccJson(QueryUtil.util.writeValueAsStringSilentAll(data))
                            .ckUser(user)
                            .cvAction("PATCH")
                            .cvTable("DQueue");
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
    @Operation(summary = "Delete DQueue")
    @DeleteMapping(value = "/DQueue", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = Void.class)))
    @PreAuthorize(Roles.ROLE_ADMIN_AUTHORITY)
    public Mono<Result> delete(@RequestBody DQueueDelete data) {
        return userService
                .getUser()
                .flatMap(user -> {
                    var tLog = TLog.builder()
                            .ccJson(QueryUtil.util.writeValueAsStringSilentAll(data))
                            .ckUser(user)
                            .cvId(data.getCkId().toString())
                            .cvAction("DELETE")
                            .cvTable("DQueue");
                    return Mono.defer(() -> service.delete(data, user))
                            .doOnSuccess(val -> auditService.save(tLog.build()))
                            .doOnError(err -> auditService.saveError(tLog.build(), err));
                });
    }
}
