package ru.tehnobear.essence.report.manage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySourcesPropertyResolver;
import org.springframework.stereotype.Component;
import ru.tehnobear.essence.dao.entries.TSource;
import ru.tehnobear.essence.report.source.EmptyPlugin;
import ru.tehnobear.essence.report.source.JDBCPlugin;
import ru.tehnobear.essence.share.exception.ReportException;
import ru.tehnobear.essence.share.plugin.SourcePlugin;
import ru.tehnobear.essence.share.util.Util;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class SourceManager {
    private Map<String, SourcePlugin> inited = new HashMap<>();
    private static final String DYNAMIC_PROPERTIES_SOURCE_NAME = "dynamicSourceProperties";
    private final ConfigurableEnvironment environment;

    public SourcePlugin getSource(TSource source) {
        if (inited.containsKey(source.getCkId())) {
            return inited.get(source.getCkId());
        }
        MutablePropertySources propertySources = environment.getPropertySources();
        var id = String.format("%s.%s", DYNAMIC_PROPERTIES_SOURCE_NAME, source.getCkId());
        if (!propertySources.contains(id)) {
            Map<String, Object> dynamicProperties = new HashMap<>();
            Util.buildFlattenedMap(dynamicProperties, Map.of(source.getCkId(), source.getCctParameter()), DYNAMIC_PROPERTIES_SOURCE_NAME);
            propertySources.addLast(new OriginTrackedMapPropertySource(id, dynamicProperties));
        }
        var resolver = new PropertySourcesPropertyResolver(propertySources);
        var property = (Map<String, Object>) resolver.getProperty(id, Map.class);
        Util.resolveMap(property, id, resolver);

        switch (source.getSourceType().getCkId()) {
            case "JDBC": {
                var plugin = new JDBCPlugin(source, property);
                inited.put(source.getCkId(), plugin);
                return plugin;
            }
            case "EMPTY": {
                var plugin = new EmptyPlugin(source, property);
                inited.put(source.getCkId(), plugin);
                return plugin;
            }
            case "PLUGIN": {
                try {
                    var clazz = Class.forName(source.getCvPlugin());
                    var constructor = clazz.getConstructor(TSource.class, Map.class);
                    var plugin = (SourcePlugin) constructor.newInstance(source, property);
                    inited.put(source.getCkId(), plugin);
                    return plugin;
                } catch (ClassNotFoundException |
                         NoSuchMethodException |
                         InvocationTargetException |
                         InstantiationException |
                         IllegalAccessException e ) {
                    log.error(e.getLocalizedMessage(), e);
                    throw ReportException.fromFormat("Not found Source {}", source.getCkId(), e);
                }
            }
            default: {
                throw ReportException.fromFormat("Not found Source {}", source.getCkId());
            }
        }
    }
}
