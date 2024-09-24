package ru.tehnobear.essence.report;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PGInterval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.support.TransactionTemplate;
import ru.tehnobear.essence.dao.dto.EStatus;
import ru.tehnobear.essence.dao.entries.QTDGlobalSetting;
import ru.tehnobear.essence.dao.entries.QTQueue;
import ru.tehnobear.essence.dao.entries.QTServerFlag;

import java.sql.SQLException;
import java.time.Instant;

@Configuration
@ConditionalOnProperty(name="app.report.enabled", havingValue = "true")
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class ReportConfiguration {
    private static final PGInterval DEFAULT_INTERVAL = new PGInterval(0, 0, 90, 0, 0, 0);
    private static final String VAR_INTERVAL_ONLINE = "INTERVAL_ONLINE";
    private static final String VAR_INTERVAL_OFFLINE = "INTERVAL_OFFLINE";
    private final JPAQueryFactory queryFactory;
    private final TransactionTemplate transactionTemplate;
    private String name;
    @Autowired
    @Qualifier("appReportName")
    public void setName(String appReportName) {
        this.name = appReportName;
        transactionTemplate.execute(status -> {
            if (queryFactory.selectFrom(QTServerFlag.tServerFlag).where(QTServerFlag.tServerFlag.ckId.eq(name)).fetchOne() == null) {
                queryFactory
                        .insert(QTServerFlag.tServerFlag)
                        .columns(QTServerFlag.tServerFlag.ckId, QTServerFlag.tServerFlag.ctChange)
                        .values(name, Instant.now())
                        .execute();
            } else {
                queryFactory
                        .update(QTServerFlag.tServerFlag)
                        .where(QTServerFlag.tServerFlag.ckId.eq(name))
                        .set(QTServerFlag.tServerFlag.ctChange, Instant.now())
                        .execute();
            }
            queryFactory
                    .update(QTQueue.tQueue)
                    .where(
                        QTQueue.tQueue.status.ckId.eq(EStatus.PROCESSING)
                        .and(QTQueue.tQueue.clOnline.eq(false))
                        .and(QTQueue.tQueue.server.ckId.eq(name))
                    )
                    .set(QTQueue.tQueue.status.ckId, EStatus.NEW)
                    .execute();
            status.flush();
            return null;
        });
    }

    @Bean
    public PGInterval onlineInterval() {
        var val = queryFactory.selectFrom(QTDGlobalSetting.tDGlobalSetting)
                .where(QTDGlobalSetting.tDGlobalSetting.ckId.eq(VAR_INTERVAL_ONLINE)
                    .and(QTDGlobalSetting.tDGlobalSetting.clDeleted.eq(false))
                )
                .fetchOne();
        if (val != null && val.getCvValue() != null && !val.getCvValue().trim().isEmpty()) {
            try {
                return new PGInterval(val.getCvValue().trim());
            } catch (SQLException e) {
                log.warn("Error parse {}", VAR_INTERVAL_ONLINE, e);
            }
        }
        return DEFAULT_INTERVAL;
    }

    @Bean
    public PGInterval offlineInterval() {
        var val = queryFactory.selectFrom(QTDGlobalSetting.tDGlobalSetting)
                .where(QTDGlobalSetting.tDGlobalSetting.ckId.eq(VAR_INTERVAL_OFFLINE)
                    .and(QTDGlobalSetting.tDGlobalSetting.clDeleted.eq(false))
                )
                .fetchOne();
        if (val != null && val.getCvValue() != null && !val.getCvValue().trim().isEmpty()) {
            try {
                return new PGInterval(val.getCvValue().trim());
            } catch (SQLException e) {
                log.warn("Error parse {}", VAR_INTERVAL_ONLINE, e);
            }
        }
        return DEFAULT_INTERVAL;
    }


    @Scheduled(cron = "*/30 * * * * *")
    public void checkServer() {
        transactionTemplate.execute(status -> {
            queryFactory
                    .update(QTServerFlag.tServerFlag)
                    .where(QTServerFlag.tServerFlag.ckId.eq(name))
                    .set(QTServerFlag.tServerFlag.ctChange, Instant.now())
                    .execute();
            status.flush();
            return null;
        });
    }
}
