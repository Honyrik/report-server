package ru.tehnobear.essence.share.plugin;

import ru.tehnobear.essence.share.dto.QueueReport;

import java.util.List;

public interface FormatPlugin {
    byte[] print(QueueReport queue, SourcePlugin source, List<ReportPlugin> reportPlugins);
}
