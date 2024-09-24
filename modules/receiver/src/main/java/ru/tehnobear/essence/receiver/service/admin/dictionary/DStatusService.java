package ru.tehnobear.essence.receiver.service.admin.dictionary;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.tehnobear.essence.dao.entries.QTDStatus;
import ru.tehnobear.essence.receiver.dto.admin.dictionary.DStatusDelete;
import ru.tehnobear.essence.receiver.dto.admin.dictionary.DStatusFetch;
import ru.tehnobear.essence.receiver.dto.admin.dictionary.DStatusInsert;
import ru.tehnobear.essence.receiver.dto.admin.dictionary.DStatusResult;
import ru.tehnobear.essence.share.dto.Result;
import ru.tehnobear.essence.share.util.QueryUtil;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class DStatusService {
    private final JPAQueryFactory queryFactory;
    private static final String ID_KEY = "ckId";
    private static final String ID_KEY_OLD = "ckIdOld";

    public Mono<DStatusResult> fetch(DStatusFetch fetch) {
        var query = queryFactory.selectFrom(QTDStatus.tDStatus);
        if (!fetch.getData().containsKey("clDeleted")) {
            fetch.getData().put("clDeleted", false);
        }
        QueryUtil.util.filterAndSort(query, fetch, QTDStatus.tDStatus);
        var res = query.fetchResults();

        return Mono.just(
            DStatusResult
                .builder()
                .total(res.getTotal())
                .data(res.getResults())
                .build()
        );
    }

    @Transactional
    public Mono<Result> insert(DStatusInsert data, String user) {
        var query = queryFactory
                .insert(QTDStatus.tDStatus);
        QueryUtil.util.initInsertValue(query, data, QTDStatus.tDStatus);
        query.columns(QTDStatus.tDStatus.ckUser);
        query.values(user);
        query.execute();

        return Mono.just(Result.builder().ckId(data.getCkId()).build());
    }

    @Transactional
    public Mono<Result> update(Map<String, Object> data, String user) {
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
        var query = queryFactory
                .update(QTDStatus.tDStatus);
        QueryUtil.util.initWhereValue(query, Map.of(ID_KEY, id), QTDStatus.tDStatus);
        QueryUtil.util.initSetValue(query, data, QTDStatus.tDStatus);
        query.set(QTDStatus.tDStatus.ckUser, user);
        query.execute();

        return Mono.just(Result.builder().ckId(id).build());
    }

    @Transactional
    public Mono<Result> delete(DStatusDelete data, String user) {
        var query = queryFactory
                .selectFrom(QTDStatus.tDStatus);
        QueryUtil.util.initWhereValue(query, data, QTDStatus.tDStatus);
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
                    .delete(QTDStatus.tDStatus);
            QueryUtil.util.initWhereValue(queryDelete, data, QTDStatus.tDStatus);

            queryDelete.execute();
        } else {
            var queryUpdate = queryFactory
                    .update(QTDStatus.tDStatus);
            QueryUtil.util.initWhereValue(queryUpdate, data, QTDStatus.tDStatus);
            queryUpdate.set(QTDStatus.tDStatus.clDeleted, true);
            queryUpdate.set(QTDStatus.tDStatus.ckUser, user);

            queryUpdate.execute();
        }

        return Mono.just(Result.builder().ckId(data.getCkId()).build());
    }
}
