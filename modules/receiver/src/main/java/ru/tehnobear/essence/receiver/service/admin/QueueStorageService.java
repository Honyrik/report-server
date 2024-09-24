package ru.tehnobear.essence.receiver.service.admin;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.tehnobear.essence.dao.entries.QTQueueStorage;
import ru.tehnobear.essence.receiver.dto.admin.QueueStorageDelete;
import ru.tehnobear.essence.receiver.dto.admin.QueueStorageFetch;
import ru.tehnobear.essence.receiver.dto.admin.QueueStorageResult;
import ru.tehnobear.essence.share.dto.Result;
import ru.tehnobear.essence.share.util.QueryUtil;

@Component
@Slf4j
@RequiredArgsConstructor
public class QueueStorageService {
    private final JPAQueryFactory queryFactory;
    private final EntityManager entityManager;
    private static final String ID_KEY = "ckId";
    private static final String ID_KEY_OLD = "ckIdOld";

    public Mono<QueueStorageResult> fetch(QueueStorageFetch fetch) {
        var query = queryFactory.selectFrom(QTQueueStorage.tQueueStorage);
        if (!fetch.getData().containsKey("clDeleted")) {
            fetch.getData().put("clDeleted", false);
        }
        QueryUtil.util.filterAndSort(query, fetch, QTQueueStorage.tQueueStorage);
        var res = query.fetchResults();

        return Mono.just(
            QueueStorageResult
                .builder()
                .total(res.getTotal())
                .data(res.getResults())
                .build()
        );
    }

    @Transactional
    public Mono<Result> delete(QueueStorageDelete data, String user) {
        var query = queryFactory
                .selectFrom(QTQueueStorage.tQueueStorage);
        QueryUtil.util.initWhereValue(query, data, QTQueueStorage.tQueueStorage);
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
                    .delete(QTQueueStorage.tQueueStorage);
            QueryUtil.util.initWhereValue(queryDelete, data, QTQueueStorage.tQueueStorage);

            queryDelete.execute();
        } else {
            var queryUpdate = queryFactory
                    .update(QTQueueStorage.tQueueStorage);
            QueryUtil.util.initWhereValue(queryUpdate, data, QTQueueStorage.tQueueStorage);
            queryUpdate.set(QTQueueStorage.tQueueStorage.clDeleted, true);
            queryUpdate.set(QTQueueStorage.tQueueStorage.ckUser, user);

            queryUpdate.execute();
        }

        return Mono.just(Result.builder().ckId(data.getCkId()).build());
    }
}
