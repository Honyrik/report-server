package ru.tehnobear.essence.report.source;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;
import ru.tehnobear.essence.dao.entries.TSource;
import ru.tehnobear.essence.share.plugin.SourcePlugin;
import ru.tehnobear.essence.share.util.Util;

import java.util.Map;

@Slf4j
public class EmptyPlugin implements SourcePlugin {
    public final String name;
    private final JREmptyDataSource dataSource;
    public EmptyPlugin(TSource source, Map<String,Object> params) {
        this.name = source.getCkId();
        var prop = Util.objectMapper.convertValue(params, EmptyProperty.class);
        dataSource = new JREmptyDataSource(prop.count);
    }

    @Override
    public JRDataSource getDataSource(Map<String, Object> param) {
        return dataSource;
    }

    private static class EmptyProperty {
        @JsonAlias({"count","row","cn_empty_row"})
        public Integer count = 0;
    }
}
