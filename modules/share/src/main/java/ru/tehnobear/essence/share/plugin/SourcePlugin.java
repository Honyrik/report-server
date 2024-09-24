package ru.tehnobear.essence.share.plugin;

import net.sf.jasperreports.engine.JRDataSource;
import ru.tehnobear.essence.share.dto.ETypeSource;
import ru.tehnobear.essence.share.exception.ReportException;

import java.sql.Connection;
import java.util.Map;

public interface SourcePlugin {
    default ETypeSource getType() {
        return ETypeSource.DATASOURCE;
    }
    default Connection getConnection(Map<String, Object> param) {
        throw new ReportException("Not support getConnection");
    }
    default JRDataSource getDataSource(Map<String, Object> param) {
        throw new ReportException("Not support getDataSource");
    }
}
