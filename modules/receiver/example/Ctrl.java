package ru.tehnobear.essence.receiver.dto.admin.dictionary.example;

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
import ru.tehnobear.essence.receiver.api.admin.AdminApi;
import ru.tehnobear.essence.receiver.dto.admin.ExampleFetch;
import ru.tehnobear.essence.receiver.dto.admin.ExampleInsert;
import ru.tehnobear.essence.receiver.dto.admin.ExampleResult;
import ru.tehnobear.essence.receiver.dto.admin.ExampleUpdate;
import ru.tehnobear.essence.receiver.dto.admin.ExampleDelete;
import ru.tehnobear.essence.share.dto.Result;
import ru.tehnobear.essence.receiver.service.admin.UserService;
import ru.tehnobear.essence.receiver.service.admin.ExampleService;
import ru.tehnobear.essence.util.Roles;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Example", description = "Управление Example")
public class ExampleCtrl implements AdminApi {
    private final ExampleService service;
    private final UserService userService;
    @Operation(summary = "Fetch Example")
    @PostMapping(value = "/Example", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = Void.class)))
    public Mono<ExampleResult> fetchExample(@RequestBody ExampleFetch fetch) {
        return service.fetch(fetch);
    }
    @Operation(summary = "Insert Example")
    @PutMapping(value = "/Example", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = Void.class)))
    @PreAuthorize(Roles.ROLE_ADMIN_AUTHORITY)
    public Mono<Result> insert(
            @RequestBody ExampleInsert data
    ) {
        return userService.getUser().flatMap(user -> service.insert(data, user));
    }
    @Operation(summary = "Update Example")
    @PatchMapping(value = "/Example", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = Void.class)))
    @PreAuthorize(Roles.ROLE_ADMIN_AUTHORITY)
    public Mono<Result> update(
            @RequestBody ExampleUpdate data
    ) {
        return userService.getUser().flatMap(user -> service.update(data, user));
    }
    @Operation(summary = "Delete Example")
    @DeleteMapping(value = "/Example", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = Void.class)))
    @PreAuthorize(Roles.ROLE_ADMIN_AUTHORITY)
    public Mono<Result> delete(@RequestBody ExampleDelete data) {
        return userService.getUser().flatMap(user -> service.delete(data, user));
    }
}
