package ru.tehnobear.essence.report.jasper;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import ru.tehnobear.essence.dao.dto.EAssetType;
import ru.tehnobear.essence.dao.entries.TDAssetType;
import ru.tehnobear.essence.dao.entries.TDFormat;
import ru.tehnobear.essence.dao.entries.TQueue;
import ru.tehnobear.essence.share.dto.ETypeSource;
import ru.tehnobear.essence.share.dto.QueueReport;
import ru.tehnobear.essence.share.exception.ReportException;
import ru.tehnobear.essence.share.plugin.FormatPlugin;
import ru.tehnobear.essence.share.plugin.JasperFormatExporter;
import ru.tehnobear.essence.share.plugin.ReportPlugin;
import ru.tehnobear.essence.share.plugin.SourcePlugin;
import ru.tehnobear.essence.share.util.Util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
public class JasperPrinter implements FormatPlugin {
    public final String name;
    private final Map<String, Object> param;
    private JasperFormatExporter jasperFormatExporter;
    public JasperPrinter(TDFormat format, Map<String, Object> param) {
        this.name = format.getCkId();
        this.param = param;
        this.jasperFormatExporter = JasperFormatExporterFactory.getExporter(format, param);
    }

    @Override
    public byte[] print(QueueReport queue, SourcePlugin source, List<ReportPlugin> reportPlugins) {
        var jasperReport = getJasperReport(queue.getQueue());
        var queueId = queue.getQueue().getCkId();
        Map<String, Object> parametersMutable = new HashMap<>(queue.getQueue().getCctParameter());
        queue
            .getReportAssets()
            .stream()
            .forEach(val -> {
                switch (val.getCvName()) {
                    case JRParameter.REPORT_VIRTUALIZER -> {
                        try {
                            var conf =
                                    Util.objectMapper.readValue(
                                            val.getAsset().getCvAsset(),
                                            JRVirtualizeSettings.class
                                    );
                            var virt = conf.getVirtualizer();
                            if (virt != null) {
                                parametersMutable.put(JRParameter.REPORT_VIRTUALIZER, virt);
                            }
                        } catch (JsonProcessingException e) {
                            throw ReportException.fromFormat("Parse asset for queue {}", queueId, e);
                        }
                    }
                    case JRParameter.REPORT_LOCALE -> {
                        var locale =
                                Locale.forLanguageTag(val.getAsset().getCvAsset());
                        if (locale != null) {
                            parametersMutable.put(JRParameter.REPORT_LOCALE, locale);
                        }
                    }
                    default -> {
                        parametersMutable.put(
                            val.getCvName(),
                            parseValue(
                                queueId,
                                val.getAsset().getType(),
                                val.getAsset().getType().getCrType() == EAssetType.BINARY ?
                                new ByteArrayInputStream(val.getAsset().getCbAsset()) :
                                val.getAsset().getCvAsset()
                            )
                        );
                    }
                }
            });
        try {
            var jasperPrint = source.getType().equals(ETypeSource.CONNECTION) ?
                    JasperFillManager.fillReport(jasperReport, parametersMutable, source.getConnection(parametersMutable)) :
                    JasperFillManager.fillReport(jasperReport, parametersMutable, source.getDataSource(parametersMutable));
            for(var plugin : reportPlugins) {
                var res = plugin.beforeExportReport(queue, source, this, jasperPrint, jasperFormatExporter);
                if (res != null) {
                    return res;
                }
            }
            return jasperFormatExporter.export(jasperPrint, queue.getQueue());
        } catch (JRException e) {
            throw ReportException.fromFormat("Error print format {} queue {}", name, queueId, e);
        }
    }

    @Nonnull
    protected JasperReport getJasperReport(TQueue queue) {
        var queueId = queue.getCkId();
        log.debug("Get jasperDesign for queue: {}", queueId);
        if (!queue.getFormat().getAsset().getType().getCkId().equalsIgnoreCase("JRXML")) {
            throw ReportException.fromFormat("Asset not JRXML queue {}", queueId);
        }

        var jasperDesign = loadDesignFromString(queueId, queue.getFormat().getAsset().getCvAsset());

        log.debug("JasperDesign load successful");
        try {
            return JasperCompileManager.compileReport(jasperDesign);
        } catch (JRException e) {
            throw ReportException.fromFormat("Error compile JRXML for queue {}", queueId, e);
        }
    }

    @Nonnull
    public static JasperDesign loadDesignFromString(UUID queueId, String xml) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))) {
            return JRXmlLoader.load(inputStream);
        } catch (IOException | JRException e) {
            throw ReportException.fromFormat("Error load JRXML for queue {}", queueId, e);
        }
    }

    private static Object parseValue(UUID queueId, TDAssetType type, Object value) {
        switch (type.getCkId()) {
            case "JRXML" -> {
                return loadDesignFromString(queueId, (String) value);
            }
            case "ARRAY" -> {
                try {
                    return Util.objectMapper.readValue((String) value, List.class);
                } catch (JsonProcessingException e) {
                    throw ReportException.fromFormat("Parse asset for queue {}", queueId, e);
                }
            }
            case "OBJECT" -> {
                try {
                    return Util.objectMapper.readValue((String) value, Map.class);
                } catch (JsonProcessingException e) {
                    throw ReportException.fromFormat("Parse asset for queue {}", queueId, e);
                }
            }
            default -> {
                return value;
            }
        }
    }
}
