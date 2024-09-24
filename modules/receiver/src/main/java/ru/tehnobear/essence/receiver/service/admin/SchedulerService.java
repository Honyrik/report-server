package ru.tehnobear.essence.receiver.service.admin;

import com.cronutils.parser.CronParser;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Mono;
import ru.tehnobear.essence.dao.entries.QTReport;
import ru.tehnobear.essence.dao.entries.QTReportFormat;
import ru.tehnobear.essence.dao.entries.QTScheduler;
import ru.tehnobear.essence.dao.entries.TScheduler;
import ru.tehnobear.essence.receiver.dto.admin.SchedulerDelete;
import ru.tehnobear.essence.receiver.dto.admin.SchedulerFetch;
import ru.tehnobear.essence.receiver.dto.admin.SchedulerInsert;
import ru.tehnobear.essence.receiver.dto.admin.SchedulerResult;
import ru.tehnobear.essence.share.dto.Result;
import ru.tehnobear.essence.share.util.QueryUtil;
import ru.tehnobear.essence.share.util.Util;

import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class SchedulerService extends AbstractService {
    private final CronParser parser;
    public SchedulerService(JPAQueryFactory queryFactory, EntityManager entityManager, TransactionTemplate transactionTemplate, CronParser reportCronParser) {
        super(queryFactory, entityManager, transactionTemplate);
        parser = reportCronParser;
    }

    public Mono<SchedulerResult> fetch(SchedulerFetch fetch) {
        var query = queryFactory.selectFrom(QTScheduler.tScheduler);
        if (!fetch.getData().containsKey("clDeleted")) {
            fetch.getData().put("clDeleted", false);
        }
        QueryUtil.util.filterAndSort(query, fetch, QTScheduler.tScheduler);
        var res = query.fetchResults();

        return Mono.just(
            SchedulerResult
                .builder()
                .total(res.getTotal())
                .data(res.getResults())
                .build()
        );
    }

    @Transactional
    public Mono<Result> insert(SchedulerInsert data, String user) {
        var result = Result
                .builder()
                .build();
        var dataInsert = Util.objectMapper.convertValue(data, TScheduler.class);
        dataInsert.setCkId(UUID.randomUUID());
        dataInsert.setCkUser(user);
        findObject(QTReport.tReport, data.getReport(), found -> {
            if (found != null) {
                dataInsert.setReport(found);
                if (dataInsert.getCnPriority() == null) {
                    dataInsert.setCnPriority(found.getCnPriority());
                }
            } else {
                result.addError("Not Found report");
            }
        });
        findObject(QTReportFormat.tReportFormat, data.getFormat(), found -> {
            if (found != null && found.getReport().getCkId().equals(dataInsert.getReport().getCkId())) {
                dataInsert.setFormat(found);
            } else {
                result.addError("Not Found format");
            }
        });
        try {
            parser.parse(data.getCvUnixCron());
        } catch (Exception e) {
            log.error(e.getLocalizedMessage(), e);
            result.addError("Error format cron");
        }


        if (result.isError() || result.isWarning()) {
            return Mono.just(result);
        }

        var res = entityManager.merge(dataInsert);
        result.setCkId(res.getCkId());
        return Mono.just(result);
    }

    @Transactional
    public Mono<Result> update(Map<String, Object> data, String user) {
        var result = Result
                .builder()
                .build();
        var id = data.remove(ID_KEY_OLD);
        if (id == null) {
            id = data.remove(ID_KEY);
        }
        if (id == null) {
            result.addError("Not Found ID");
        }
        if (data.containsKey("report")) {
            findObject(QTReport.tReport, data.get("report"), found -> {
                if (found != null) {
                    data.put("report", Map.of(ID_KEY, found.getCkId()));
                } else {
                    result.addError("Not Found report");
                }
            });
        }

        if (data.containsKey("format")) {
            findObject(QTReportFormat.tReportFormat, data.get("format"), found -> {
                if (found != null) {
                    data.put("format", Map.of(ID_KEY, found.getCkId()));
                } else {
                    result.addError("Not Found format");
                }
            });
        }

        if (data.containsKey("cvUnixCron")) {
            try {
                parser.parse((String) data.get("cvUnixCron"));
            } catch (Exception e) {
                log.error(e.getLocalizedMessage(), e);
                result.addError("Error format cron");
            }
        }


        if (result.isError() || result.isWarning()) {
            return Mono.just(result);
        }
        var query = queryFactory
                .update(QTScheduler.tScheduler);
        QueryUtil.util.initWhereValue(query, Map.of(ID_KEY, id), QTScheduler.tScheduler);
        QueryUtil.util.initSetValue(query, data, QTScheduler.tScheduler);
        query.set(QTScheduler.tScheduler.ckUser, user);
        query.execute();

        result.setCkId(id);
        return Mono.just(result);
    }

    @Transactional
    public Mono<Result> delete(SchedulerDelete data, String user) {
        var query = queryFactory
                .selectFrom(QTScheduler.tScheduler);
        QueryUtil.util.initWhereValue(query, data, QTScheduler.tScheduler);
        var value = query.fetchOne();
        if (value == null) {
            return Mono.just(
                Result
                    .builder()
                    .ckId(data.getCkId())
                    .build()
                    .addError("Not Found")
            );
        }
        if (value.isClDeleted()) {
            var queryDelete = queryFactory
                    .delete(QTScheduler.tScheduler);
            QueryUtil.util.initWhereValue(queryDelete, data, QTScheduler.tScheduler);

            queryDelete.execute();
        } else {
            var queryUpdate = queryFactory
                    .update(QTScheduler.tScheduler);
            QueryUtil.util.initWhereValue(queryUpdate, data, QTScheduler.tScheduler);
            queryUpdate.set(QTScheduler.tScheduler.clDeleted, true);
            queryUpdate.set(QTScheduler.tScheduler.ckUser, user);

            queryUpdate.execute();
        }

        return Mono.just(Result.builder().ckId(data.getCkId()).build());
    }
}
