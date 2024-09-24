package ru.tehnobear.essence.receiver.service.admin;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Mono;
import ru.tehnobear.essence.dao.entries.*;
import ru.tehnobear.essence.receiver.dto.admin.ReportFormatDelete;
import ru.tehnobear.essence.receiver.dto.admin.ReportFormatFetch;
import ru.tehnobear.essence.receiver.dto.admin.ReportFormatInsert;
import ru.tehnobear.essence.receiver.dto.admin.ReportFormatResult;
import ru.tehnobear.essence.share.dto.Result;
import ru.tehnobear.essence.share.util.QueryUtil;
import ru.tehnobear.essence.share.util.Util;

import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class ReportFormatService extends AbstractService {
    public ReportFormatService(JPAQueryFactory queryFactory, EntityManager entityManager, TransactionTemplate transactionTemplate) {
        super(queryFactory, entityManager, transactionTemplate);
    }

    public Mono<ReportFormatResult> fetch(ReportFormatFetch fetch) {
        var query = queryFactory.selectFrom(QTReportFormat.tReportFormat);
        if (!fetch.getData().containsKey("clDeleted")) {
            fetch.getData().put("clDeleted", false);
        }
        QueryUtil.util.filterAndSort(query, fetch, QTReportFormat.tReportFormat);
        var res = query.fetchResults();

        return Mono.just(
            ReportFormatResult
                .builder()
                .total(res.getTotal())
                .data(res.getResults())
                .build()
        );
    }

    @Transactional
    public Mono<Result> insert(ReportFormatInsert data, String user) {
        var result = Result
                .builder()
                .build();
        var dataInsert = Util.objectMapper.convertValue(data, TReportFormat.class);
        dataInsert.setCkId(UUID.randomUUID());
        dataInsert.setCkUser(user);
        findObject(QTAsset.tAsset, data.getAsset(), found -> {
            if (found != null) {
                dataInsert.setAsset(found);
            } else {
                result.addError("Not Found asset");
            }
        });
        findObject(QTReport.tReport, data.getReport(), found -> {
            if (found != null) {
                dataInsert.setReport(found);
            } else {
                result.addError("Not Found report");
            }
        });
        findObject(QTDFormat.tDFormat, data.getFormat(), found -> {
            if (found != null) {
                dataInsert.setFormat(found);
            } else {
                result.addError("Not Found format");
            }
        });
        findObject(QTSource.tSource, data.getSource(), found -> {
            if (found != null) {
                dataInsert.setSource(found);
            } else {
                result.addError("Not Found source");
            }
        });

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
        if (data.containsKey("asset")) {
            findObject(QTAsset.tAsset, data.get("asset"), found -> {
                if (found != null) {
                    data.put("asset", Map.of(ID_KEY, found.getCkId()));
                } else {
                    result.addError("Not Found asset");
                }
            });
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
            findObject(QTDFormat.tDFormat, data.get("format"), found -> {
                if (found != null) {
                    data.put("format", Map.of(ID_KEY, found.getCkId()));
                } else {
                    result.addError("Not Found format");
                }
            });
        }

        if (data.containsKey("source")) {
            findObject(QTSource.tSource, data.get("source"), found -> {
                if (found != null) {
                    data.put("source", Map.of(ID_KEY, found.getCkId()));
                } else {
                    result.addError("Not Found source");
                }
            });
        }

        if (result.isError() || result.isWarning()) {
            return Mono.just(result);
        }
        var query = queryFactory
                .update(QTReportFormat.tReportFormat);
        QueryUtil.util.initWhereValue(query, Map.of(ID_KEY, id), QTReportFormat.tReportFormat);
        QueryUtil.util.initSetValue(query, data, QTReportFormat.tReportFormat);
        query.set(QTReportFormat.tReportFormat.ckUser, user);
        query.execute();

        result.setCkId(id);
        return Mono.just(result);
    }

    @Transactional
    public Mono<Result> delete(ReportFormatDelete data, String user) {
        var query = queryFactory
                .selectFrom(QTReportFormat.tReportFormat);
        QueryUtil.util.initWhereValue(query, data, QTReportFormat.tReportFormat);
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
                    .delete(QTReportFormat.tReportFormat);
            QueryUtil.util.initWhereValue(queryDelete, data, QTReportFormat.tReportFormat);

            queryDelete.execute();
        } else {
            var queryUpdate = queryFactory
                    .update(QTReportFormat.tReportFormat);
            QueryUtil.util.initWhereValue(queryUpdate, data, QTReportFormat.tReportFormat);
            queryUpdate.set(QTReportFormat.tReportFormat.clDeleted, true);
            queryUpdate.set(QTReportFormat.tReportFormat.ckUser, user);

            queryUpdate.execute();
        }

        return Mono.just(Result.builder().ckId(data.getCkId()).build());
    }
}
