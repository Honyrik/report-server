package ru.tehnobear.essence.share.plugin;

import net.sf.jasperreports.engine.JasperPrint;
import ru.tehnobear.essence.share.dto.FileStore;
import ru.tehnobear.essence.share.dto.QueueReport;

public interface ReportPlugin {
    default byte[] beforeReport(QueueReport queueReport, SourcePlugin sourcePlugin, FormatPlugin formatPlugin) {
        return null;
    }
    default byte[] beforeExportReport(QueueReport queueReport, SourcePlugin sourcePlugin, FormatPlugin formatPlugin, JasperPrint jasperPrint, JasperFormatExporter jasperFormatExporter) {
        return null;
    }
    default FileStore afterReport(QueueReport queueReport, FileStore file) {
        return file;
    }
}
