package ru.tehnobear.essence.receiver.ctrl.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.tehnobear.essence.receiver.api.admin.AdminApi;
import ru.tehnobear.essence.receiver.dto.admin.QueueLogFetch;
import ru.tehnobear.essence.receiver.dto.admin.QueueLogResult;
import ru.tehnobear.essence.receiver.service.admin.QueueLogService;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin QueueLog", description = "Управление QueueLog")
public class QueueLogCtrl implements AdminApi {
    private final QueueLogService service;
    @Operation(summary = "Fetch QueueLog")
    @PostMapping(value = "/QueueLog", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = Void.class)))
    public Mono<QueueLogResult> fetchQueueLog(@RequestBody QueueLogFetch fetch) {
        return service.fetch(fetch);
    }
}
