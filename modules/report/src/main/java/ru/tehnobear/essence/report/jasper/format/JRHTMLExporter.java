package ru.tehnobear.essence.report.jasper.format;

import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterConfiguration;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import ru.tehnobear.essence.dao.entries.TDFormat;
import ru.tehnobear.essence.dao.entries.TQueue;
import ru.tehnobear.essence.share.plugin.JasperFormatExporter;
import ru.tehnobear.essence.share.exception.ReportException;
import ru.tehnobear.essence.share.util.Util;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class JRHTMLExporter implements JasperFormatExporter {
    public final String name;
    private final Map<String, Object> param;
    private TDFormat data;
    public JRHTMLExporter(TDFormat format, Map<String, Object> param) {
        this.name = format.getCkId();
        this.param = param;
        this.data = format;
    }
    @Override
    public byte[] export(JasperPrint jasperPrint, TQueue queue) {
        try {
            var exporter = new HtmlExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            var byteArrayOutputStream = new ByteArrayOutputStream();
            exporter.setExporterOutput(new SimpleHtmlExporterOutput(byteArrayOutputStream));
            var property = new HashMap<>(param);
            property.putAll(queue.getCctParameter());
            var configuration = Util.objectMapper.convertValue(property, SimpleHtmlExporterConfiguration.class);
            exporter.setConfiguration(configuration);
            exporter.exportReport();

            return byteArrayOutputStream.toByteArray();
        } catch (JRException e) {
            throw ReportException.fromFormat("Error export {} queue {}", name, queue.getCkId(), e);
        }
    }
}
