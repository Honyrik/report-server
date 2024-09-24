package ru.tehnobear.essence.receiver.dto.admin.dictionary.example;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Mono;
import ru.tehnobear.essence.dao.entries.QTExample;
import ru.tehnobear.essence.dao.entries.TExample;
import ru.tehnobear.essence.receiver.dto.admin.ExampleFetch;
import ru.tehnobear.essence.receiver.dto.admin.ExampleInsert;
import ru.tehnobear.essence.receiver.dto.admin.ExampleResult;
import ru.tehnobear.essence.receiver.dto.admin.ExampleDelete;
import ru.tehnobear.essence.receiver.service.admin.AbstractService;
import ru.tehnobear.essence.share.dto.Result;
import ru.tehnobear.essence.share.util.QueryUtil;

import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class ExampleService extends AbstractService {
    public ExampleService(JPAQueryFactory queryFactory, EntityManager entityManager, TransactionTemplate transactionTemplate) {
        super(queryFactory, entityManager, transactionTemplate);
    }

    public Mono<ExampleResult> fetch(ExampleFetch fetch) {
        var query = queryFactory.selectFrom(QTExample.tExample);
        QueryUtil.util.filterAndSort(query, fetch, QTExample.tExample);
        var res = query.fetchResults();

        return Mono.just(
            ExampleResult
                .builder()
                .total(res.getTotal())
                .data(res.getResults())
                .build()
        );
    }

    @Transactional
    public Mono<Result> insert(ExampleInsert data, String user) {
        var result = Result
                .builder()
                .build();
        var dataInsert = QueryUtil.util.objectMapper.convertValue(data, TExample.class);
        dataInsert.setCkId(UUID.randomUUID());
        dataInsert.setCkUser(user);

        if (result.isError() || result.isWarning()) {
            return Mono.just(result);
        }
        var res = entityManager.merge(dataInsert);

        result.setCkId(id);
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

        if (result.isError() || result.isWarning()) {
            return Mono.just(result);
        }
        var query = queryFactory
                .update(QTExample.tExample);
        QueryUtil.util.initWhereValue(query, Map.of(ID_KEY, id), QTExample.tExample);
        QueryUtil.util.initSetValue(query, data, QTExample.tExample);
        query.set(QTExample.tExample.ckUser, user);
        query.execute();

        result.setCkId(id);
        return Mono.just(result);
    }

    @Transactional
    public Mono<Result> delete(ExampleDelete data, String user) {
        var result = Result
                .builder()
                .build();
        var query = queryFactory
                .selectFrom(QTExample.tExample);
        QueryUtil.util.initWhereValue(query, data, QTExample.tExample);
        var value = query.fetchOne();
        if (value == null) {
            result.addError("Not Found");
        }

        if (result.isError() || result.isWarning()) {
            return Mono.just(result);
        }
        if (value.getClDeleted()) {
            var queryDelete = queryFactory
                    .delete(QTExample.tExample);
            QueryUtil.util.initWhereValue(queryDelete, data, QTExample.tExample);

            queryDelete.execute();
        } else {
            var queryUpdate = queryFactory
                    .update(QTExample.tExample);
            QueryUtil.util.initWhereValue(queryUpdate, data, QTExample.tExample);
            queryUpdate.set(QTExample.tExample.clDeleted, true);
            queryUpdate.set(QTExample.tExample.ckUser, user);

            queryUpdate.execute();
        }

        result.setCkId(data.getCkId());
        return Mono.just(result);
    }
}
