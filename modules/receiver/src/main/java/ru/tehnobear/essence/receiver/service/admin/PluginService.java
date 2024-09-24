package ru.tehnobear.essence.receiver.service.admin;

import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.tehnobear.essence.receiver.dto.admin.dictionary.DictionaryResult;
import ru.tehnobear.essence.share.dto.Dictionary;
import ru.tehnobear.essence.share.plugin.*;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class PluginService {
    private static final String DEFAULT_PACKAGE = "ru.tehnobear.essence";
    private final Reflections reflection;

    public PluginService(
            @Value("#{'${app.receiver.pluginPackageScan}'.split(',')}")
            String[] arr
    ) {
        var packagePath = Set.of(arr)
                .stream().filter(val -> val != null && !val.trim().isEmpty())
                .map(String::trim)
                .collect(Collectors.toSet());
        if (packagePath.contains(DEFAULT_PACKAGE)) {
            packagePath.add(DEFAULT_PACKAGE);
        }
        this.reflection = new Reflections(packagePath);
    }

    public Mono<DictionaryResult> fetchAuthorization() {
        return Mono.just(DictionaryResult.builder().data(reflection.getSubTypesOf(AuthorizationPlugin.class)
                .stream()
                .map(val -> Dictionary.builder()
                        .id(val.getName()).build())
                .toList()).build());
    }

    public Mono<DictionaryResult> fetchFormat() {
        return Mono.just(DictionaryResult.builder().data(reflection.getSubTypesOf(FormatPlugin.class)
                .stream()
                .map(val -> Dictionary.builder()
                        .id(val.getName()).build())
                .toList()).build());
    }

    public Mono<DictionaryResult> fetchSource() {
        return Mono.just(DictionaryResult.builder().data(reflection.getSubTypesOf(SourcePlugin.class)
                .stream()
                .map(val -> Dictionary.builder()
                        .id(val.getName()).build())
                .toList()).build());
    }

    public Mono<DictionaryResult> fetchStorage() {
        return Mono.just(DictionaryResult.builder().data(reflection.getSubTypesOf(StoragePlugin.class)
                .stream()
                .map(val -> Dictionary.builder()
                        .id(val.getName()).build())
                .toList()).build());
    }

    public Mono<DictionaryResult> fetchJasperFormatExporter() {
        return Mono.just(DictionaryResult.builder().data(reflection.getSubTypesOf(JasperFormatExporter.class)
                .stream()
                .map(val -> Dictionary.builder()
                        .id(val.getName()).build())
                .toList()).build());
    }

    public Mono<DictionaryResult> fetchReportPlugin() {
        return Mono.just(DictionaryResult.builder().data(reflection.getSubTypesOf(ReportPlugin.class)
                .stream()
                .map(val -> Dictionary.builder()
                        .id(val.getName()).build())
                .toList()).build());
    }
}
