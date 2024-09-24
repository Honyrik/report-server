package ru.tehnobear.essence.receiver.ctrl.admin.dictionary;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.tehnobear.essence.receiver.api.admin.DictionaryApi;
import ru.tehnobear.essence.receiver.dto.admin.dictionary.DictionaryResult;
import ru.tehnobear.essence.receiver.service.admin.PluginService;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Dictionary Admin Plugin", description = "Справочник плагинов")
public class PluginCtrl implements DictionaryApi {
    private final PluginService service;
    @Operation(summary = "Fetch Authorization Plugin")
    @GetMapping(value = "/plugin/authorization", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = Void.class)))
    public Mono<DictionaryResult> fetchAuthorization() {
        return service.fetchAuthorization();
    }
    @Operation(summary = "Fetch Format Plugin")
    @GetMapping(value = "/plugin/format", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = Void.class)))
    public Mono<DictionaryResult> fetchFormat() {
        return service.fetchFormat();
    }
    @Operation(summary = "Fetch Source Plugin")
    @GetMapping(value = "/plugin/source", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = Void.class)))
    public Mono<DictionaryResult> fetchSource() {
        return service.fetchSource();
    }
    @Operation(summary = "Fetch Storage Plugin")
    @GetMapping(value = "/plugin/storage", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = Void.class)))
    public Mono<DictionaryResult> fetchStorage() {
        return service.fetchStorage();
    }
    @Operation(summary = "Fetch JasperFormatExporter Plugin")
    @GetMapping(value = "/plugin/jasperFormatExporter", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = Void.class)))
    public Mono<DictionaryResult> fetchJasperFormatExporter() {
        return service.fetchJasperFormatExporter();
    }
    @Operation(summary = "Fetch Report Plugin")
    @GetMapping(value = "/plugin/report", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = Void.class)))
    public Mono<DictionaryResult> fetchReportPlugin() {
        return service.fetchReportPlugin();
    }
}
