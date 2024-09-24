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
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.tehnobear.essence.dao.dto.EAssetType;
import ru.tehnobear.essence.dao.entries.TLog;
import ru.tehnobear.essence.receiver.api.admin.AdminApi;
import ru.tehnobear.essence.receiver.dto.admin.*;
import ru.tehnobear.essence.receiver.service.admin.AssetService;
import ru.tehnobear.essence.receiver.service.admin.UserService;
import ru.tehnobear.essence.share.dto.Result;
import ru.tehnobear.essence.share.exception.ResultException;
import ru.tehnobear.essence.share.service.AuditService;
import ru.tehnobear.essence.share.util.QueryUtil;
import ru.tehnobear.essence.share.util.Util;
import ru.tehnobear.essence.util.Roles;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Asset", description = "Управление Asset")
public class AssetCtrl implements AdminApi {
    private final AssetService service;
    private final UserService userService;
    private final AuditService auditService;
    @Operation(summary = "Fetch Asset")
    @PostMapping(value = "/Asset", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = Void.class)))
    public Mono<AssetResult> fetchAsset(@RequestBody AssetFetch fetch) {
        return service.fetch(fetch);
    }
    @Operation(summary = "Fetch Asset")
    @PostMapping(value = "/Asset/download", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = Void.class)))
    public Mono<ResponseEntity<byte[]>> fetchAssetDownload(@RequestBody AssetFetch fetch) {
        return service.fetch(fetch)
                .map(val -> {
                    var first = val.getData().stream().findFirst().get();

                    return ResponseEntity
                            .ok()
                            .header("Content-Type", first.getType().getCvContentType())
                            .header(
                                    "Content-Disposition",
                                    String.format(
                                            "attachment; filename*=UTF-8''%s%s",
                                            URLEncoder.encode(first.getCvName(), StandardCharsets.UTF_8),
                                            first.getType().getCvExtension()
                                    )
                            )
                            .body(
                            first.getType().getCrType() == EAssetType.TEXT ?
                                    first.getCvAsset().getBytes(StandardCharsets.UTF_8) : first.getCbAsset()
                    );
                });
    }
    @Operation(summary = "Insert Asset")
    @PutMapping(value = "/Asset", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = Void.class)))
    @PreAuthorize(Roles.ROLE_ADMIN_AUTHORITY)
    public Mono<Result> insert(
            @RequestBody AssetInsert data
    ) {
        return userService
                .getUser()
                .flatMap(user -> {
                    var tLog = TLog.builder()
                            .ccJson(QueryUtil.util.writeValueAsStringSilentAll(data))
                            .ckUser(user)
                            .cvAction("PUT")
                            .cvTable("Asset");
                    return Mono.defer(() -> service.insert(data, user))
                            .doOnSuccess(val -> auditService.save(tLog.cvId(Optional.ofNullable(val.getCkId()).orElse("").toString()).build()))
                            .doOnError(err -> auditService.saveError(tLog.build(), err));
                });
    }
    @Operation(
            summary = "Insert Asset Upload",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = {
                            @Content(
                                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                                    schema = @Schema(
                                            implementation = AssetInsertUpload.AssetInsertUploadSchema.class
                                    )
                            )
                    }
            )
    )
    @PutMapping(value = "/Asset/upload", produces = {MediaType.APPLICATION_JSON_VALUE}, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = Void.class)))
    @PreAuthorize(Roles.ROLE_ADMIN_AUTHORITY)
    public Mono<Result> insertUpload(
            ServerWebExchange exchange
    ) {
        return exchange.getMultipartData().flatMap(data -> {
                var upload = Optional.ofNullable(data.get("upload"))
                        .flatMap(val -> val.stream().findFirst()).get();
                var jsonObj = Util.util.partToInputStream(Optional.ofNullable(data.get("json"))
                        .flatMap(val -> val.stream().findFirst()).get())
                        .map(val -> {
                            try {
                                return Util.objectMapper.readValue(val, AssetInsert.class);
                            } catch (IOException e) {
                                throw new ResultException(Result.builder().build().addError(e.getMessage()));
                            }
                        });

                return jsonObj.map(json -> new AssetInsertUpload(json, upload));
            })
            .flatMap(data -> userService.getUser()
                .flatMap(user -> {
                    var tLog = TLog.builder()
                            .ccJson(QueryUtil.util.writeValueAsStringSilentAll(data.getJson()))
                            .ckUser(user)
                            .cvAction("PUT")
                            .cvTable("Asset");
                    return Mono.defer(() -> service
                                    .insertUpload(
                                            data,
                                            user
                                    ))
                            .doOnSuccess(val -> auditService.save(tLog.cvId(Optional.ofNullable(val.getCkId()).orElse("").toString()).build()))
                            .doOnError(err -> auditService.saveError(tLog.build(), err));
                }
                ));
    }
    @Operation(summary = "Update Asset")
    @PatchMapping(value = "/Asset", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = Void.class)))
    @PreAuthorize(Roles.ROLE_ADMIN_AUTHORITY)
    public Mono<Result> update(
            @RequestBody AssetUpdate data
    ) {
        return userService
                .getUser()
                .flatMap(user -> {
                    var tLog = TLog.builder()
                            .ccJson(QueryUtil.util.writeValueAsStringSilentAll(data))
                            .ckUser(user)
                            .cvAction("PATCH")
                            .cvTable("Asset");
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
    @Operation(
            summary = "Update Asset Upload",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = {
                            @Content(
                                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                                    schema = @Schema(
                                            implementation = AssetUpdateUpload.AssetUpdateUploadSchema.class
                                    )
                            )
                    }
            )
    )
    @PatchMapping(value = "/Asset/upload", produces = {MediaType.APPLICATION_JSON_VALUE}, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = Void.class)))
    @PreAuthorize(Roles.ROLE_ADMIN_AUTHORITY)
    public Mono<Result> updateUpload(
            ServerWebExchange exchange
    ) {
        return exchange.getMultipartData().flatMap(data -> {
                    var upload = Optional.ofNullable(data.get("upload"))
                            .flatMap(val -> val.stream().findFirst()).get();
                    var jsonObj = Util.util.partToInputStream(Optional.ofNullable(data.get("json"))
                                    .flatMap(val -> val.stream().findFirst()).get())
                            .map(val -> {
                                try {
                                    return Util.objectMapper.readValue(val, AssetUpdate.class);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });

                    return jsonObj.map(json -> new AssetUpdateUpload(json, upload));
                })
                .flatMap(data -> userService.getUser()
                        .flatMap(user -> {
                            var tLog = TLog.builder()
                                    .ccJson(QueryUtil.util.writeValueAsStringSilentAll(data.getJson()))
                                    .ckUser(user)
                                    .cvAction("PATCH")
                                    .cvTable("Asset");
                            return Mono.defer(() -> service
                                    .updateUpload(
                                            data,
                                            user
                                    ))
                                    .doOnSuccess(val -> auditService.save(tLog.cvId(Optional.ofNullable(val.getCkId()).orElse("").toString()).build()))
                                    .doOnError(err -> auditService.saveError(
                                            tLog.cvId(
                                                    Optional.ofNullable(data.getJson().get("ckIdOld"))
                                                            .orElse(Optional.ofNullable(data.getJson().get("ckId")).orElse(""))
                                                            .toString()
                                            ).build(),
                                            err
                                    ));
                            }
                        ));
    }
    @Operation(summary = "Delete Asset")
    @DeleteMapping(value = "/Asset", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = Void.class)))
    @PreAuthorize(Roles.ROLE_ADMIN_AUTHORITY)
    public Mono<Result> delete(@RequestBody AssetDelete data) {
        return userService
                .getUser()
                .flatMap(user -> {
                    var tLog = TLog.builder()
                            .ccJson(QueryUtil.util.writeValueAsStringSilentAll(data))
                            .ckUser(user)
                            .cvId(data.getCkId().toString())
                            .cvAction("DELETE")
                            .cvTable("Asset");
                    return Mono.defer(() -> service.delete(data, user))
                            .doOnSuccess(val -> auditService.save(tLog.build()))
                            .doOnError(err -> auditService.saveError(tLog.build(), err));
                });
    }
}
