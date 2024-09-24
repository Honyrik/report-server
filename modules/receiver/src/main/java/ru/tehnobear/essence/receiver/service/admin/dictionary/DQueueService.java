package ru.tehnobear.essence.receiver.service.admin.dictionary;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Mono;
import ru.tehnobear.essence.dao.entries.QTDQueue;
import ru.tehnobear.essence.dao.entries.TDQueue;
import ru.tehnobear.essence.receiver.dto.admin.dictionary.*;
import ru.tehnobear.essence.receiver.service.admin.AbstractService;
import ru.tehnobear.essence.share.dto.EFilter;
import ru.tehnobear.essence.share.dto.Filter;
import ru.tehnobear.essence.share.dto.Result;
import ru.tehnobear.essence.share.util.QueryUtil;
import ru.tehnobear.essence.share.util.Util;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class DQueueService extends AbstractService {
    public DQueueService(JPAQueryFactory queryFactory, EntityManager entityManager, TransactionTemplate transactionTemplate) {
        super(queryFactory, entityManager, transactionTemplate);
    }

    public Mono<DQueueResult> fetch(DQueueFetch fetch) {
        var query = queryFactory.selectFrom(QTDQueue.tDQueue);
        if (fetch.getData() != null) {
            var ckParent = fetch.getData().get("ckParent");
            if (ckParent != null) {
                fetch.getData().put("parent", Map.of("ckId", ckParent));
            }
        }
        if (!fetch.getData().containsKey("clDeleted")) {
            fetch.getData().put("clDeleted", false);
        }
        QueryUtil.util.filterAndSort(query, fetch, QTDQueue.tDQueue);
        var res = query.fetchResults();

        return Mono.just(
            DQueueResult
                .builder()
                .total(res.getTotal())
                .data(res.getResults())
                .build()
        );
    }

    public Mono<DQueueListResult> fetchTreeList(DQueueFetch fetch) {
        var query = queryFactory.selectFrom(QTDQueue.tDQueue);
        if (fetch.getData() != null) {
            var ckParent = fetch.getData().get("ckParent");
            if (ckParent != null) {
                fetch.getData().put("parent", Map.of("ckId", ckParent));
            }
        }
        if ((fetch.getData() == null || fetch.getData().isEmpty()) &&
                (fetch.getFilter() == null || fetch.getFilter().isEmpty())) {
           fetch.setFilter(List.of(Filter.builder().operator(EFilter.NULL).property("parent").build()));
        }
        QueryUtil.util.filterAndSort(query, fetch, QTDQueue.tDQueue);
        var finalFetch = fetch;
        var data = query
                .fetch()
                .stream()
                .map(val -> DQueueListResult.TDQueueList.fromTDQueue(val, finalFetch))
                .collect(
                        () -> new HashSet<DQueueListResult.TDQueueList>(),
                        (list, item) -> list.addAll(item),
                        (list1, list2) -> list1.addAll(list2)
                );

        return Mono.just(
                DQueueListResult
                        .builder()
                        .total((long) data.size())
                        .data(data)
                        .build()
        );
    }

    @Transactional
    public Mono<Result> insert(DQueueInsert data, String user) {
        var result = Result
                .builder().build();
        var dataInsert = Util.objectMapper.convertValue(data, TDQueue.class);
        dataInsert.setCkUser(user);
        var isParent = data.getParent() != null;
        if (isParent || data.getCkParent() != null) {
            findObject(
                QTDQueue.tDQueue,
                isParent ? data.getParent() : Map.of(ID_KEY, data.getCkParent()),
                found -> {
                    if (found != null) {
                        dataInsert.setParent(found);
                    } else {
                        result.addError("Not Found parent");
                    }
                }
            );
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
        var isParent = data.containsKey("parent");
        if (isParent || data.containsKey("ckParent")) {
            findObject(
                    QTDQueue.tDQueue,
                    isParent ? data.get("parent") : Map.of(ID_KEY, data.get("ckParent")),
                    found -> {
                if (found != null) {
                    data.put("parent", Map.of(ID_KEY, found.getCkId()));
                } else {
                    result.addError("Not Found parent");
                }
            });
        }
        if (result.isError() || result.isWarning()) {
            return Mono.just(result);
        }
        var query = queryFactory
                .update(QTDQueue.tDQueue);
        QueryUtil.util.initWhereValue(query, Map.of(ID_KEY, id), QTDQueue.tDQueue);
        QueryUtil.util.initSetValue(query, data, QTDQueue.tDQueue);
        query.set(QTDQueue.tDQueue.ckUser, user);

        query.execute();

        result.setCkId(id);
        return Mono.just(result);
    }

    @Transactional
    public Mono<Result> delete(DQueueDelete data, String user) {
        var query = queryFactory
                .selectFrom(QTDQueue.tDQueue);
        QueryUtil.util.initWhereValue(query, data, QTDQueue.tDQueue);
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
                    .delete(QTDQueue.tDQueue);
            QueryUtil.util.initWhereValue(queryDelete, data, QTDQueue.tDQueue);

            queryDelete.execute();
        } else {
            var queryUpdate = queryFactory
                    .update(QTDQueue.tDQueue);
            QueryUtil.util.initWhereValue(queryUpdate, data, QTDQueue.tDQueue);
            queryUpdate.set(QTDQueue.tDQueue.clDeleted, true);
            queryUpdate.set(QTDQueue.tDQueue.ckUser, user);

            queryUpdate.execute();
        }

        return Mono.just(Result.builder().ckId(data.getCkId()).build());
    }
}
