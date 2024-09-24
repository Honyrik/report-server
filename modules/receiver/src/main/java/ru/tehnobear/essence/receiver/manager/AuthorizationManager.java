package ru.tehnobear.essence.receiver.manager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySourcesPropertyResolver;
import org.springframework.stereotype.Component;
import ru.tehnobear.essence.dao.entries.TAuthorization;
import ru.tehnobear.essence.share.exception.ReportException;
import ru.tehnobear.essence.share.plugin.AuthorizationPlugin;
import ru.tehnobear.essence.share.util.Util;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuthorizationManager {
    private Map<UUID, AuthorizationPlugin> inited = new HashMap<>();
    private static final String DYNAMIC_PROPERTIES_SOURCE_NAME = "dynamicAuthorizationProperties";
    private final ConfigurableEnvironment environment;

    public AuthorizationPlugin getAuthorization(TAuthorization authorization) {
        if (inited.containsKey(authorization.getCkId())) {
            return inited.get(authorization.getCkId());
        }
        MutablePropertySources propertySources = environment.getPropertySources();
        var id = String.format("%s.%s", DYNAMIC_PROPERTIES_SOURCE_NAME, authorization.getCkId());
        if (!propertySources.contains(id)) {
            Map<String, Object> dynamicProperties = new HashMap<>();
            Util.buildFlattenedMap(dynamicProperties, Map.of(authorization.getCkId().toString(), authorization.getCctParameter()), DYNAMIC_PROPERTIES_SOURCE_NAME);
            propertySources.addLast(new OriginTrackedMapPropertySource(id, dynamicProperties));
        }
        var resolver = new PropertySourcesPropertyResolver(propertySources);
        var property = (Map<String, Object>) resolver.getProperty(id, Map.class);
        Util.resolveMap(property, id, resolver);

        try {
            var clazz = Class.forName(authorization.getCvPlugin());
            var constructor = clazz.getConstructor(TAuthorization.class, Map.class);
            var plugin = (AuthorizationPlugin) constructor.newInstance(authorization, property);
            inited.put(authorization.getCkId(), plugin);
            return plugin;
        } catch (ClassNotFoundException |
                 NoSuchMethodException |
                 InvocationTargetException |
                 InstantiationException |
                 IllegalAccessException e ) {
            log.error(e.getLocalizedMessage(), e);
            throw ReportException.fromFormat("Not found format for authorization {}", authorization.getCvName(), e);
        }
    }
}
