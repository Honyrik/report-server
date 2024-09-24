package ru.tehnobear.essence.report.source;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.postgresql.PGProperty;
import ru.tehnobear.essence.dao.DBProperties;
import ru.tehnobear.essence.dao.entries.TSource;
import ru.tehnobear.essence.share.dto.ETypeSource;
import ru.tehnobear.essence.share.exception.ReportException;
import ru.tehnobear.essence.share.plugin.SourcePlugin;
import ru.tehnobear.essence.share.util.Util;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static net.ttddyy.dsproxy.listener.logging.Log4jLogLevel.*;

@Slf4j
public class JDBCPlugin implements SourcePlugin {
    private final String SQL_LOGGER = "ru.tehnobear.essence.report.source.JDBCPlugin";
    public final String name;
    private final DataSource dataSource;
    public JDBCPlugin(TSource source, Map<String,Object> params) {
        this.name = source.getCkId();
        var dbProperties = Util.objectMapper.convertValue(params, DBProperties.class);
        dbProperties.setRegisterMbeans(true);
        dbProperties.setAutoCommit(false);
        if (dbProperties.getDriverClassName().equalsIgnoreCase("org.postgresql.Driver")) {
            dbProperties.addDataSourceProperty(PGProperty.REWRITE_BATCHED_INSERTS.getName(), true);
        }
        HikariDataSource ds = new HikariDataSource(dbProperties);

        dataSource = ProxyDataSourceBuilder.create(ds)
                .countQuery()
                .logQueryByLog4j(TRACE, SQL_LOGGER)
                .logSlowQueryByLog4j(600, TimeUnit.SECONDS, WARN, SQL_LOGGER)
                .logSlowQueryByLog4j(1200, TimeUnit.SECONDS, ERROR, SQL_LOGGER)
                .multiline()
                .build();
    }

    @Override
    public ETypeSource getType() {
        return ETypeSource.CONNECTION;
    }

    @Override
    public Connection getConnection(Map<String, Object> param) {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            log.error("Plugin: {}, {}", name, e.getLocalizedMessage(), e);
            throw new ReportException(e.getLocalizedMessage());
        }
    }
}
