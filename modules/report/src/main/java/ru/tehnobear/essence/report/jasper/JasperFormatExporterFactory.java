package ru.tehnobear.essence.report.jasper;

import lombok.extern.slf4j.Slf4j;
import ru.tehnobear.essence.dao.entries.TDFormat;
import ru.tehnobear.essence.share.exception.ReportException;
import ru.tehnobear.essence.share.plugin.JasperFormatExporter;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

@Slf4j
public class JasperFormatExporterFactory {
    public static JasperFormatExporter getExporter(TDFormat format, Map<String, Object> property) {
        try {
            var clazz = Class.forName(format.getCvPlugin());
            var constructor = clazz.getConstructor(TDFormat.class, Map.class);
            return  (JasperFormatExporter) constructor.newInstance(format, property);
        } catch (ClassNotFoundException |
                 NoSuchMethodException |
                 InvocationTargetException |
                 InstantiationException |
                 IllegalAccessException e ) {
            log.error(e.getLocalizedMessage(), e);
            throw ReportException.fromFormat("Not found format {}", format.getCkId(), e);
        }
    }
}
