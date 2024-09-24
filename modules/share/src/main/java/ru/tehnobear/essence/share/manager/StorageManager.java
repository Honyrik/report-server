package ru.tehnobear.essence.share.manager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.tehnobear.essence.dao.entries.QTDGlobalSetting;
import ru.tehnobear.essence.share.plugin.StoragePlugin;
import ru.tehnobear.essence.share.store.AwsStorage;
import ru.tehnobear.essence.share.store.DBStorage;
import ru.tehnobear.essence.share.store.DirStorage;
import ru.tehnobear.essence.share.util.Util;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class StorageManager {
    private static final String VAR_SETTING = "STORAGE";
    private static final String VAR_SETTING_TYPE = VAR_SETTING + "_TYPE";
    private final StoragePlugin plugin;
    public StorageManager(JPAQueryFactory queryFactory, DBStorage dbStorage) {
        StoragePlugin plugin = dbStorage;
        var typeObj = queryFactory.selectFrom(QTDGlobalSetting.tDGlobalSetting)
                .where(
                    QTDGlobalSetting.tDGlobalSetting.ckId.eq(VAR_SETTING_TYPE)
                    .and(QTDGlobalSetting.tDGlobalSetting.clDeleted.eq(false))
                )
                .fetchOne();
        if (typeObj == null || typeObj.getCvValue().trim().isEmpty()) {
            this.plugin = plugin;
            return;
        }
        var type = typeObj.getCvValue().trim().toUpperCase();
        var setting = Optional.ofNullable(
                        queryFactory.selectFrom(QTDGlobalSetting.tDGlobalSetting)
                                .where(
                                    QTDGlobalSetting.tDGlobalSetting.ckId.eq(String.format("%s_%s", VAR_SETTING, type))
                                    .and(QTDGlobalSetting.tDGlobalSetting.clDeleted.eq(false))
                                )
                                .fetchOne()
                )
                .map(val -> {
                    if (val.getCvValue() == null || val.getCvValue().trim().isEmpty()) {
                        return null;
                    }
                    try {
                        return Util.objectMapper.readValue(val.getCvValue(), Map.class);
                    } catch (JsonProcessingException e) {
                        log.error(e.getLocalizedMessage(), e);
                        return null;
                    }
                })
                .orElse(new HashMap<String, Object>());
        try {
            switch (type) {
                case "DIR": {
                    plugin = new DirStorage(setting);
                    break;
                }
                case "AWS":
                case "MINIO":
                case "RIAKCS": {
                    plugin = new AwsStorage(setting);
                    break;
                }
                case "PLUGIN": {
                    try {
                        var clazz = Class.forName((String) setting.remove("plugin"));
                        var constructor = clazz.getConstructor(Map.class);
                        plugin = (StoragePlugin) constructor.newInstance(setting);
                    } catch (ClassNotFoundException |
                             NoSuchMethodException |
                             InvocationTargetException |
                             InstantiationException |
                             IllegalAccessException e) {
                        log.error("Not found StoragePlugin {}", type, e);
                    }
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Not load StoragePlugin {}", type, e);
        }
        this.plugin = plugin;
    }


    public StoragePlugin getStorage() {
        return plugin;
    }
}
