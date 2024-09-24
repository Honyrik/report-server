package ru.tehnobear.essence.receiver.service.admin;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Mono;
import ru.tehnobear.essence.dao.entries.QTDSourceType;
import ru.tehnobear.essence.dao.entries.QTSource;
import ru.tehnobear.essence.dao.entries.TSource;
import ru.tehnobear.essence.receiver.dto.admin.SourceDelete;
import ru.tehnobear.essence.receiver.dto.admin.SourceFetch;
import ru.tehnobear.essence.receiver.dto.admin.SourceInsert;
import ru.tehnobear.essence.receiver.dto.admin.SourceResult;
import ru.tehnobear.essence.share.dto.Result;
import ru.tehnobear.essence.share.util.QueryUtil;
import ru.tehnobear.essence.share.util.Util;

import java.util.Map;

@Component
@Slf4j
public class SourceService extends AbstractService {
    public SourceService(JPAQueryFactory queryFactory, EntityManager entityManager, TransactionTemplate transactionTemplate) {
        super(queryFactory, entityManager, transactionTemplate);
    }

    public Mono<SourceResult> fetch(SourceFetch fetch) {
        var query = queryFactory.selectFrom(QTSource.tSource);
        if (!fetch.getData().containsKey("clDeleted")) {
            fetch.getData().put("clDeleted", false);
        }
        QueryUtil.util.filterAndSort(query, fetch, QTSource.tSource);
        var res = query.fetchResults();

        return Mono.just(
            SourceResult
                .builder()
                .total(res.getTotal())
                .data(res.getResults())
                .build()
        );
    }

    @Transactional
    public Mono<Result> insert(SourceInsert data, String user) {
        var result = Result
                .builder().build();
        var dataInsert = Util.objectMapper.convertValue(data, TSource.class);
        dataInsert.setCkUser(user);
        findObject(QTDSourceType.tDSourceType, data.getSourceType(), found -> {
            if (found != null) {
                dataInsert.setSourceType(found);
            } else {
                result.addError("Not Found sourceType");
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
                .builder().build();
        var id = data.remove(ID_KEY_OLD);
        if (id == null) {
            id = data.remove(ID_KEY);
        }
        if (id == null) {
            return Mono.just(
                    result
                    .addError("Not Found ID")
            );
        }
        if (data.containsKey("sourceType")) {
            findObject(QTDSourceType.tDSourceType, data.get("sourceType"), found -> {
                if (found != null) {
                    data.put("sourceType", Map.of(ID_KEY, found.getCkId()));
                } else {
                    result.addError("Not Found sourceType");
                }
            });
        }
        if (result.isError() || result.isWarning()) {
            return Mono.just(result);
        }
        var query = queryFactory
                .update(QTSource.tSource);
        QueryUtil.util.initWhereValue(query, Map.of(ID_KEY, id), QTSource.tSource);
        QueryUtil.util.initSetValue(query, data, QTSource.tSource);
        query.set(QTSource.tSource.ckUser, user);

        query.execute();

        result.setCkId(id);
        return Mono.just(result);
    }

    @Transactional
    public Mono<Result> delete(SourceDelete data, String user) {
        var query = queryFactory
                .selectFrom(QTSource.tSource);
        QueryUtil.util.initWhereValue(query, data, QTSource.tSource);
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
                    .delete(QTSource.tSource);
            QueryUtil.util.initWhereValue(queryDelete, data, QTSource.tSource);

            queryDelete.execute();
        } else {
            var queryUpdate = queryFactory
                    .update(QTSource.tSource);
            QueryUtil.util.initWhereValue(queryUpdate, data, QTSource.tSource);
            queryUpdate.set(QTSource.tSource.clDeleted, true);
            queryUpdate.set(QTSource.tSource.ckUser, user);

            queryUpdate.execute();
        }

        return Mono.just(Result.builder().ckId(data.getCkId()).build());
    }
}
