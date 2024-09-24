package ru.tehnobear.essence.share.plugin;

import net.sf.jasperreports.engine.JasperPrint;
import ru.tehnobear.essence.dao.entries.TQueue;

public interface JasperFormatExporter {
    byte[] export(JasperPrint jasperPrint, TQueue queue);
}
