package ru.tehnobear.essence.report.manage;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySourcesPropertyResolver;
import org.springframework.stereotype.Component;
import ru.tehnobear.essence.dao.entries.TReportAsset;
import ru.tehnobear.essence.share.exception.ReportException;
import ru.tehnobear.essence.share.plugin.ReportPlugin;
import ru.tehnobear.essence.share.util.Util;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class ReportPluginManager {
    private Map<UUID, ReportPlugin> inited = new HashMap<>();
    private static final String DYNAMIC_PROPERTIES_SOURCE_NAME = "dynamicReportPluginProperties";
    private final ConfigurableEnvironment environment;

    private ReportPlugin getPlugin(TReportAsset asset) {
        if (inited.containsKey(asset.getCkId())) {
            return inited.get(asset.getCkId());
        }
        MutablePropertySources propertySources = environment.getPropertySources();
        var id = String.format("%s.%s", DYNAMIC_PROPERTIES_SOURCE_NAME, asset.getCkId());
        Map<String, Object> param = new HashMap<>();
        if (asset.getAsset().getCvAsset() != null && !asset.getAsset().getCvAsset().trim().isEmpty()) {
            try {
                param = Util.objectMapper.readValue(asset.getAsset().getCvAsset(), Map.class);
            } catch (JsonProcessingException e) {
                throw ReportException.fromFormat("Error parse asset plugin {}", asset.getAsset().getCvName());
            }
        }
        if (!propertySources.contains(id)) {
            Map<String, Object> dynamicProperties = new HashMap<>();
            Util.buildFlattenedMap(dynamicProperties, Map.of(asset.getCkId().toString(), param), DYNAMIC_PROPERTIES_SOURCE_NAME);
            propertySources.addLast(new OriginTrackedMapPropertySource(id, dynamicProperties));
        }
        var resolver = new PropertySourcesPropertyResolver(propertySources);
        var property = (Map<String, Object>) resolver.getProperty(id, Map.class);
        Util.resolveMap(property, id, resolver);

        try {
            var clazz = Class.forName(asset.getAsset().getCvPlugin());
            var constructor = clazz.getConstructor(TReportAsset.class, Map.class);
            var plugin = (ReportPlugin) constructor.newInstance(asset, property);
            inited.put(asset.getCkId(), plugin);
            return plugin;
        } catch (ClassNotFoundException |
                 NoSuchMethodException |
                 InvocationTargetException |
                 InstantiationException |
                 IllegalAccessException e ) {
            log.error(e.getLocalizedMessage(), e);
            throw ReportException.fromFormat("Not found report asset plugin {}", asset.getCkId(), e);
        }
    }

    public ReportPluginResult getPlugin(List<TReportAsset> reportAssets) {
        var res = ReportPluginResult.builder()
                .plugins(new ArrayList<>())
                .assets(new ArrayList<>())
                .build();
        reportAssets.forEach(val -> {
            if (val.getAsset().getType().getCkId().equals("PLUGIN")) {
                res.getPlugins().add(getPlugin(val));
            } else {
                res.getAssets().add(val);
            }
        });
        return res;
    }


    @Data
    @Builder
    public static class ReportPluginResult {
        private List<ReportPlugin> plugins;
        private List<TReportAsset> assets;
    }
}
