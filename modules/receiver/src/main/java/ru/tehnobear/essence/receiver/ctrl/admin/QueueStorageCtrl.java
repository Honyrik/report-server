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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.tehnobear.essence.dao.entries.TLog;
import ru.tehnobear.essence.receiver.api.admin.AdminApi;
import ru.tehnobear.essence.receiver.dto.admin.QueueStorageDelete;
import ru.tehnobear.essence.receiver.dto.admin.QueueStorageFetch;
import ru.tehnobear.essence.receiver.dto.admin.QueueStorageResult;
import ru.tehnobear.essence.receiver.service.admin.QueueStorageService;
import ru.tehnobear.essence.receiver.service.admin.UserService;
import ru.tehnobear.essence.share.dto.Result;
import ru.tehnobear.essence.share.service.AuditService;
import ru.tehnobear.essence.share.util.QueryUtil;
import ru.tehnobear.essence.util.Roles;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin QueueStorage", description = "Управление QueueStorage")
public class QueueStorageCtrl implements AdminApi {
    private final QueueStorageService service;
    private final UserService userService;
    private final AuditService auditService;
    @Operation(summary = "Fetch QueueStorage")
    @PostMapping(value = "/QueueStorage", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = Void.class)))
    public Mono<QueueStorageResult> fetchQueueStorage(@RequestBody QueueStorageFetch fetch) {
        return service.fetch(fetch);
    }
    @Operation(summary = "Delete QueueStorage")
    @DeleteMapping(value = "/QueueStorage", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = Void.class)))
    @PreAuthorize(Roles.ROLE_ADMIN_AUTHORITY)
    public Mono<Result> delete(@RequestBody QueueStorageDelete data) {
        return userService
                .getUser()
                .flatMap(user -> {
                    var tLog = TLog.builder()
                            .ccJson(QueryUtil.util.writeValueAsStringSilentAll(data))
                            .ckUser(user)
                            .cvId(data.getCkId().toString())
                            .cvAction("DELETE")
                            .cvTable("QueueStorage");
                    return Mono.defer(() -> service.delete(data, user))
                            .doOnSuccess(val -> auditService.save(tLog.build()))
                            .doOnError(err -> auditService.saveError(tLog.build(), err));
                });
    }
}
