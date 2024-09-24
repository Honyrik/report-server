package ru.tehnobear.essence.report.manage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySourcesPropertyResolver;
import org.springframework.stereotype.Component;
import ru.tehnobear.essence.dao.entries.TDFormat;
import ru.tehnobear.essence.report.jasper.JasperPrinter;
import ru.tehnobear.essence.share.exception.ReportException;
import ru.tehnobear.essence.share.plugin.FormatPlugin;
import ru.tehnobear.essence.share.util.Util;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class FormatManager {
    private Map<String, FormatPlugin> inited = new HashMap<>();
    private static final String DYNAMIC_PROPERTIES_SOURCE_NAME = "dynamicFormatProperties";
    private final ConfigurableEnvironment environment;

    public FormatPlugin getFormat(TDFormat format) {
        if (inited.containsKey(format.getCkId())) {
            return inited.get(format.getCkId());
        }
        MutablePropertySources propertySources = environment.getPropertySources();
        var id = String.format("%s.%s", DYNAMIC_PROPERTIES_SOURCE_NAME, format.getCkId());
        if (!propertySources.contains(id)) {
            Map<String, Object> dynamicProperties = new HashMap<>();
            Util.buildFlattenedMap(dynamicProperties, Map.of(format.getCkId(), format.getCctParameter()), DYNAMIC_PROPERTIES_SOURCE_NAME);
            propertySources.addLast(new OriginTrackedMapPropertySource(id, dynamicProperties));
        }
        var resolver = new PropertySourcesPropertyResolver(propertySources);
        var property = (Map<String, Object>) resolver.getProperty(id, Map.class);
        Util.resolveMap(property, id, resolver);

        switch (format.getCrType()) {
            case JRXML: {
                var plugin = new JasperPrinter(format, property);
                inited.put(format.getCkId(), plugin);
                return plugin;
            }
            case PLUGIN: {
                try {
                    var clazz = Class.forName(format.getCvPlugin());
                    var constructor = clazz.getConstructor(TDFormat.class, Map.class);
                    var plugin = (FormatPlugin) constructor.newInstance(format, property);
                    inited.put(format.getCkId(), plugin);
                    return plugin;
                } catch (ClassNotFoundException |
                         NoSuchMethodException |
                         InvocationTargetException |
                         InstantiationException |
                         IllegalAccessException e ) {
                    log.error(e.getLocalizedMessage(), e);
                    throw ReportException.fromFormat("Not found format {}", format.getCkId(), e);
                }
            }
            default: {
                throw ReportException.fromFormat("Not found format {}", format.getCkId());
            }
        }
    }
}
