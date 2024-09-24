package ru.tehnobear.essence.report.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.postgresql.util.PGInterval;
import org.springdoc.webflux.ui.SwaggerIndexTransformer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Mono;
import ru.tehnobear.essence.dao.dto.EStatus;
import ru.tehnobear.essence.dao.entries.QTQueue;
import ru.tehnobear.essence.dao.entries.QTReportAsset;
import ru.tehnobear.essence.dao.entries.TQueue;
import ru.tehnobear.essence.dao.entries.TQueueLog;
import ru.tehnobear.essence.report.manage.FormatManager;
import ru.tehnobear.essence.report.manage.ReportPluginManager;
import ru.tehnobear.essence.report.manage.SourceManager;
import ru.tehnobear.essence.share.dto.FileStore;
import ru.tehnobear.essence.share.dto.QueueReport;
import ru.tehnobear.essence.share.dto.ReportBody;
import ru.tehnobear.essence.share.dto.Result;
import ru.tehnobear.essence.share.exception.ForbiddenException;
import ru.tehnobear.essence.share.exception.ReportException;
import ru.tehnobear.essence.share.manager.StorageManager;
import ru.tehnobear.essence.share.plugin.ReportPlugin;
import ru.tehnobear.essence.share.util.Util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Calendar;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class ReportRunnerService {

    private final SwaggerIndexTransformer indexPageTransformer;

    private final String HMAC_ALG = "HmacSHA512";
    private final JPAQueryFactory queryFactory;
    private final FormatManager formatManager;
    private final SourceManager sourceManager;
    private final TransactionTemplate transactionTemplate;
    private final EntityManager entityManager;
    private final String appReportName;
    private final StorageManager storageManager;
    private final PGInterval onlineInterval;
    private final PGInterval offlineInterval;
    private final ReportPluginManager reportPluginManager;
    @Value("${app.report.secret}")
    public String secret;
    public Mono<Result> run(ReportBody reportBody) {
        var result = Result
                .builder()
                .ckId(reportBody.getCkId())
                .build();
        try {
            String sign = Util.util.hmac(HMAC_ALG, reportBody.getCkId().toString(), secret);
            if (!reportBody.getSing().equalsIgnoreCase(sign)) {
                throw new ForbiddenException("Not valid sign");
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new ReportException("Not check sign", e);
        }
        var queue = transactionTemplate.execute(status -> {
            var res = queryFactory
                    .selectFrom(QTQueue.tQueue)
                    .where(QTQueue.tQueue.ckId.eq(reportBody.getCkId())
                            .and(QTQueue.tQueue.clDeleted.eq(false))
                            .and(QTQueue.tQueue.report.clDeleted.eq(false))
                            .and(QTQueue.tQueue.status.ckId.eq(EStatus.NEW))
                    )
                    .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                    .setHint("jakarta.persistence.lock.timeout", "-2")
                    .fetchOne();
            if (res != null) {
                onStart(res);
            }
            status.flush();
            return res;
        });
        if (queue == null) {
            result.addError("Not found queue");
        }
        if (result.isError()) {
            return Mono.just(result);
        }

        return onRunQueue(queue);
    }


    public Mono<Result> onRunQueue(TQueue queue) {
        var plugins = reportPluginManager.getPlugin(queryFactory
                .selectFrom(QTReportAsset.tReportAsset)
                .where(
                        QTReportAsset
                                .tReportAsset
                                .report
                                .eq(queue.getReport())
                                .and(QTReportAsset
                                    .tReportAsset.format.isNull().or(QTReportAsset
                                    .tReportAsset.format.eq(queue.getFormat()))
                                )
                                .and(QTReportAsset.tReportAsset.clDeleted.eq(false)
                                .and(QTReportAsset.tReportAsset.asset.clDeleted.eq(false)))
                )
                .orderBy(QTReportAsset
                        .tReportAsset.cvName.asc())
                .fetch());
        var obj = QueueReport.builder()
            .queue(queue)
            .reportAssets(plugins.getAssets())
            .build();
        return Mono.defer(() -> runQueue(obj, plugins.getPlugins()))
            .doOnSuccess(val -> {
                onEnd(queue);
            })
            .doOnError(err -> {
                onError(err, queue);
            })
            .then(Mono.just(
                Result
                    .builder()
                    .ckId(queue.getQueue().getCkId())
                    .build()
            ));
    }

    private void onStart(TQueue queue) {
        queryFactory.update(QTQueue.tQueue)
            .where(QTQueue.tQueue.eq(queue))
            .set(QTQueue.tQueue.server.ckId, appReportName)
            .set(QTQueue.tQueue.status.ckId, EStatus.PROCESSING)
            .set(QTQueue.tQueue.ctSt, Instant.now()).execute();
    }

    private void onEnd(TQueue queue) {
        transactionTemplate.execute(status -> {
            var clearDate = queue.getCtCleaning();
            if (clearDate == null) {
                var interval = queue.isClOnline() ? onlineInterval : offlineInterval;
                if (queue.isClOnline() && queue.getReport().getCvDurationExpireStorageOnline() != null) {
                    try {
                        interval = new PGInterval(queue.getReport().getCvDurationExpireStorageOnline());
                    } catch (SQLException e) {
                        log.warn("Error parse interval for report {}", queue.getReport(), e);
                    }
                } else if (!queue.isClOnline() && queue.getReport().getCvDurationExpireStorageOffline() != null) {
                    try {
                        interval = new PGInterval(queue.getReport().getCvDurationExpireStorageOffline());
                    } catch (SQLException e) {
                        log.warn("Error parse interval for report {}", queue.getReport(), e);
                    }
                }
                var date = Calendar.getInstance();
                interval.add(date);
                clearDate = date.toInstant();
            }
            queryFactory.update(QTQueue.tQueue)
                    .where(QTQueue.tQueue.eq(queue))
                    .set(QTQueue.tQueue.status.ckId, EStatus.SUCCESS)
                    .set(QTQueue.tQueue.ctEn, Instant.now())
                    .set(QTQueue.tQueue.ctCleaning, clearDate)
                    .execute();
            status.flush();
            return null;
        });
    }

    private void onError(Throwable err, TQueue queue) {
        transactionTemplate.execute(status -> {
            var error = TQueueLog.builder()
                    .cvError(err.getLocalizedMessage())
                    .queue(queue)
                    .ckUser(appReportName)
                    .cvErrorStacktrace(ExceptionUtils.getStackTrace(err))
                    .build();
            entityManager.merge(error);
            queryFactory.update(QTQueue.tQueue)
                    .where(QTQueue.tQueue.eq(queue))
                    .set(QTQueue.tQueue.status.ckId, EStatus.FAULT)
                    .set(QTQueue.tQueue.ctEn, Instant.now()).execute();
            status.flush();
            return null;
        });
    }

    private Mono<Void> runQueue(QueueReport queue, List<ReportPlugin> reportPlugins) {
        var source = sourceManager.getSource(queue.getQueue().getFormat().getSource());
        var format = formatManager.getFormat(queue.getQueue().getFormat().getFormat());
        byte[] data = null;
        for(var plugin : reportPlugins) {
            data = plugin.beforeReport(queue, source,format);
            if (data != null) {
                break;
            }
        }
        if (data == null) {
            data = format.print(queue, source, reportPlugins);
        }
        var file = FileStore.builder()
                .file(data)
                .name(String.format("%s%s",
                        URLEncoder.encode(
                                queue.getQueue().getCvReportName() == null ?
                                        queue.getQueue().getReport().getCvName() :
                                        queue.getQueue().getCvReportName(),
                                StandardCharsets.UTF_8
                        ),
                        queue.getQueue().getFormat().getFormat().getCvExtension()
                ))
                .contentType(queue.getQueue().getFormat().getFormat().getCvContentType())
                .build();
        for(var plugin : reportPlugins) {
            file = plugin.afterReport(queue, file);
        }
        storageManager.getStorage().saveFile(queue.getQueue(), file);
        return Mono.empty();
    }
}
