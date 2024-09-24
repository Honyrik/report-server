package ru.tehnobear.essence.receiver.service.admin;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PGInterval;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Mono;
import ru.tehnobear.essence.dao.entries.QTAuthorization;
import ru.tehnobear.essence.dao.entries.QTDQueue;
import ru.tehnobear.essence.dao.entries.QTReport;
import ru.tehnobear.essence.dao.entries.TReport;
import ru.tehnobear.essence.receiver.dto.admin.ReportDelete;
import ru.tehnobear.essence.receiver.dto.admin.ReportFetch;
import ru.tehnobear.essence.receiver.dto.admin.ReportInsert;
import ru.tehnobear.essence.receiver.dto.admin.ReportResult;
import ru.tehnobear.essence.share.dto.Result;
import ru.tehnobear.essence.share.util.QueryUtil;
import ru.tehnobear.essence.share.util.Util;

import java.sql.SQLException;
import java.util.Map;

@Component
@Slf4j
public class ReportService extends AbstractService {
    private static final String ID_CURRENT_AUTHORIZATION = "683b27d2-0ae3-45a1-ac1b-aaace8e378f6";

    public ReportService(JPAQueryFactory queryFactory, EntityManager entityManager, TransactionTemplate transactionTemplate) {
        super(queryFactory, entityManager, transactionTemplate);
    }

    public Mono<ReportResult> fetch(ReportFetch fetch) {
        var query = queryFactory.selectFrom(QTReport.tReport);
        if (!fetch.getData().containsKey("clDeleted")) {
            fetch.getData().put("clDeleted", false);
        }
        QueryUtil.util.filterAndSort(query, fetch, QTReport.tReport);
        var res = query.fetchResults();

        return Mono.just(
            ReportResult
                .builder()
                .total(res.getTotal())
                .data(res.getResults())
                .build()
        );
    }

    @Transactional
    public Mono<Result> insert(ReportInsert data, String user) {
        var result = Result
                .builder().build();
        var dataInsert = Util.objectMapper.convertValue(data, TReport.class);
        dataInsert.setCkUser(user);
        findObject(QTAuthorization.tAuthorization, data.getAuthorization(), found -> {
            if (found != null) {
                dataInsert.setAuthorization(found);
            } else {
                findObject(QTAuthorization.tAuthorization, Map.of(ID_KEY, ID_CURRENT_AUTHORIZATION), foundCurrent -> {
                    if (foundCurrent != null) {
                        dataInsert.setAuthorization(foundCurrent);
                    } else {
                        result.addError("Not Found authorization");
                    }
                });
            }
        });
        findObject(QTDQueue.tDQueue, data.getQueue(), found -> {
            if (found != null) {
                dataInsert.setQueue(found);
            } else {
                findObject(QTDQueue.tDQueue, Map.of(ID_KEY, "default"), foundCurrent -> {
                    if (foundCurrent != null) {
                        dataInsert.setQueue(foundCurrent);
                    } else {
                        result.addError("Not Found queue");
                    }
                });
            }
        });
        if (data.getCvDurationExpireStorageOffline() != null) {
            try {
                new PGInterval(data.getCvDurationExpireStorageOffline());
            } catch (SQLException e) {
                log.error(e.getLocalizedMessage(), e);
                result.addError("Fail format cvDurationExpireStorageOffline");
            }
        }
        if (data.getCvDurationExpireStorageOnline() != null) {
            try {
                new PGInterval(data.getCvDurationExpireStorageOnline());
            } catch (SQLException e) {
                log.error(e.getLocalizedMessage(), e);
                result.addError("Fail format getCvDurationExpireStorageOnline");
            }
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
                .builder().build();
        var id = data.remove(ID_KEY_OLD);
        if (id == null) {
            id = data.remove(ID_KEY);
        }
        if (id == null) {
            return Mono.just(
                Result
                    .builder()
                    .build()
                    .addError("Not Found ID")
            );
        }

        if (data.containsKey("authorization")) {
            findObject(
                QTAuthorization.tAuthorization,
                data.get("authorization"),
                found -> {
                if (found != null) {
                    data.put("authorization", Map.of(ID_KEY, found.getCkId()));
                } else {
                    result.addError("Not Found authorization");
                }
            });
        }
        if (data.containsKey("queue")) {
            findObject(QTDQueue.tDQueue, data.get("queue"), found -> {
                if (found != null) {
                    data.put("queue", Map.of(ID_KEY, found.getCkId()));
                } else {
                    result.addError("Not Found queue");
                }
            });
        }
        if (data.containsKey("cvDurationExpireStorageOffline")) {
            try {
                new PGInterval((String) data.get("cvDurationExpireStorageOffline"));
            } catch (SQLException e) {
                log.error(e.getLocalizedMessage(), e);
                result.addError("Fail format cvDurationExpireStorageOffline");
            }
        }
        if (data.containsKey("cvDurationExpireStorageOnline")) {
            try {
                new PGInterval((String) data.get("cvDurationExpireStorageOnline"));
            } catch (SQLException e) {
                log.error(e.getLocalizedMessage(), e);
                result.addError("Fail format cvDurationExpireStorageOnline");
            }
        }
        if (result.isError() || result.isWarning()) {
            return Mono.just(result);
        }

        var query = queryFactory
                .update(QTReport.tReport);
        QueryUtil.util.initWhereValue(query, Map.of(ID_KEY, id), QTReport.tReport);
        QueryUtil.util.initSetValue(query, data, QTReport.tReport);
        query.set(QTReport.tReport.ckUser, user);

        query.execute();

        result.setCkId(id);
        return Mono.just(result);
    }

    @Transactional
    public Mono<Result> delete(ReportDelete data, String user) {
        var query = queryFactory
                .selectFrom(QTReport.tReport);
        QueryUtil.util.initWhereValue(query, data, QTReport.tReport);
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
                    .delete(QTReport.tReport);
            QueryUtil.util.initWhereValue(queryDelete, data, QTReport.tReport);

            queryDelete.execute();
        } else {
            var queryUpdate = queryFactory
                    .update(QTReport.tReport);
            QueryUtil.util.initWhereValue(queryUpdate, data, QTReport.tReport);
            queryUpdate.set(QTReport.tReport.clDeleted, true);
            queryUpdate.set(QTReport.tReport.ckUser, user);

            queryUpdate.execute();
        }

        return Mono.just(Result.builder().ckId(data.getCkId()).build());
    }
}
