package ru.tehnobear.essence.receiver.service.admin;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Mono;
import ru.tehnobear.essence.dao.entries.*;
import ru.tehnobear.essence.receiver.dto.admin.ReportAssetDelete;
import ru.tehnobear.essence.receiver.dto.admin.ReportAssetFetch;
import ru.tehnobear.essence.receiver.dto.admin.ReportAssetInsert;
import ru.tehnobear.essence.receiver.dto.admin.ReportAssetResult;
import ru.tehnobear.essence.share.dto.Result;
import ru.tehnobear.essence.share.util.QueryUtil;
import ru.tehnobear.essence.share.util.Util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class ReportAssetService extends AbstractService {
    public ReportAssetService(JPAQueryFactory queryFactory, EntityManager entityManager, TransactionTemplate transactionTemplate) {
        super(queryFactory, entityManager, transactionTemplate);
    }

    public Mono<ReportAssetResult> fetch(ReportAssetFetch fetch) {
        var query = queryFactory.selectFrom(QTReportAsset.tReportAsset);
        if (!fetch.getData().containsKey("clDeleted")) {
            fetch.getData().put("clDeleted", false);
        }
        QueryUtil.util.filterAndSort(query, fetch, QTReportAsset.tReportAsset);
        var res = query.fetchResults();

        return Mono.just(
            ReportAssetResult
                .builder()
                .total(res.getTotal())
                .data(res.getResults())
                .build()
        );
    }

    @Transactional
    public Mono<Result> insert(ReportAssetInsert data, String user) {
        var result = Result
                .builder()
                .build();
        var dataInsert = Util.objectMapper.convertValue(data, TReportAsset.class);
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

        if (dataInsert.getFormat() != null) {
            findObject(QTReportFormat.tReportFormat, data.getFormat(), dataInsert::setFormat);
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
            findObject(QTReportFormat.tReportFormat, data.get("format"), found -> {
                if (found != null) {
                    data.put("format", Map.of(ID_KEY, found.getCkId()));
                } else {
                    var map = new HashMap<>();
                    map.put(ID_KEY, null);
                    data.put("format", map);
                }
            });
        }

        if (result.isError() || result.isWarning()) {
            return Mono.just(result);
        }
        var query = queryFactory
                .update(QTReportAsset.tReportAsset);
        QueryUtil.util.initWhereValue(query, Map.of(ID_KEY, id), QTReportAsset.tReportAsset);
        QueryUtil.util.initSetValue(query, data, QTReportAsset.tReportAsset);
        query.set(QTReportAsset.tReportAsset.ckUser, user);

        query.execute();

        result.setCkId(id);
        return Mono.just(result);
    }

    @Transactional
    public Mono<Result> delete(ReportAssetDelete data, String user) {
        var query = queryFactory
                .selectFrom(QTReportAsset.tReportAsset);
        QueryUtil.util.initWhereValue(query, data, QTReportAsset.tReportAsset);
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
                    .delete(QTReportAsset.tReportAsset);
            QueryUtil.util.initWhereValue(queryDelete, data, QTReportAsset.tReportAsset);

            queryDelete.execute();
        } else {
            var queryUpdate = queryFactory
                    .update(QTReportAsset.tReportAsset);
            QueryUtil.util.initWhereValue(queryUpdate, data, QTReportAsset.tReportAsset);
            queryUpdate.set(QTReportAsset.tReportAsset.clDeleted, true);
            queryUpdate.set(QTReportAsset.tReportAsset.ckUser, user);

            queryUpdate.execute();
        }

        return Mono.just(Result.builder().ckId(data.getCkId()).build());
    }
}
