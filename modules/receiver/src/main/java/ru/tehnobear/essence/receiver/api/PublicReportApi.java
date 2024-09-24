package ru.tehnobear.essence.receiver.api;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.tehnobear.essence.receiver.dto.admin.PublicReportBody;
import ru.tehnobear.essence.share.web.ReportExceptionHandler;

import java.util.UUID;

@Tag(name = "Public", description = "Публичное апи")
@RequestMapping("/public")
@ReportExceptionHandler
public interface PublicReportApi {
    @Operation(summary = "Run report")
    @PostMapping(value = "/run")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = Void.class)))
    Mono<ResponseEntity<Object>> run(ServerWebExchange exchange, @RequestBody PublicReportBody data);
    @Operation(summary = "Get file")
    @GetMapping(value = "/getFile/{id}")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = Void.class)))
    Mono<ResponseEntity<Object>> getFile(ServerWebExchange exchange, @PathVariable UUID id);
}
