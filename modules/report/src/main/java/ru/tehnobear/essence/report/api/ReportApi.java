package ru.tehnobear.essence.report.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;
import ru.tehnobear.essence.share.dto.ReportBody;
import ru.tehnobear.essence.share.dto.Result;
import ru.tehnobear.essence.share.web.ReportExceptionHandler;

@Tag(name = "Report", description = "Печать")
@RequestMapping("/report")
@ReportExceptionHandler
@ConditionalOnProperty(name="app.report.enabled", havingValue = "true")
public interface ReportApi {
    @Operation(summary = "Run report")
    @PostMapping(value = "/run", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = Void.class)))
    Mono<Result> run(@RequestBody ReportBody data);
}
