package ru.tehnobear.essence.receiver.service.admin.dictionary;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.tehnobear.essence.dao.entries.QTDSourceType;
import ru.tehnobear.essence.receiver.dto.admin.dictionary.DSourceTypeDelete;
import ru.tehnobear.essence.receiver.dto.admin.dictionary.DSourceTypeFetch;
import ru.tehnobear.essence.receiver.dto.admin.dictionary.DSourceTypeInsert;
import ru.tehnobear.essence.receiver.dto.admin.dictionary.DSourceTypeResult;
import ru.tehnobear.essence.share.dto.Result;
import ru.tehnobear.essence.share.util.QueryUtil;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class DSourceTypeService {
    private final JPAQueryFactory queryFactory;
    private static final String ID_KEY = "ckId";
    private static final String ID_KEY_OLD = "ckIdOld";

    public Mono<DSourceTypeResult> fetch(DSourceTypeFetch fetch) {
        var query = queryFactory.selectFrom(QTDSourceType.tDSourceType);
        if (!fetch.getData().containsKey("clDeleted")) {
            fetch.getData().put("clDeleted", false);
        }
        QueryUtil.util.filterAndSort(query, fetch, QTDSourceType.tDSourceType);
        var res = query.fetchResults();

        return Mono.just(
            DSourceTypeResult
                .builder()
                .total(res.getTotal())
                .data(res.getResults())
                .build()
        );
    }

    @Transactional
    public Mono<Result> insert(DSourceTypeInsert data, String user) {
        var query = queryFactory
                .insert(QTDSourceType.tDSourceType);
        QueryUtil.util.initInsertValue(query, data, QTDSourceType.tDSourceType);
        query.columns(QTDSourceType.tDSourceType.ckUser);
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
                .update(QTDSourceType.tDSourceType);
        QueryUtil.util.initWhereValue(query, Map.of(ID_KEY, id), QTDSourceType.tDSourceType);
        QueryUtil.util.initSetValue(query, data, QTDSourceType.tDSourceType);
        query.set(QTDSourceType.tDSourceType.ckUser, user);

        query.execute();

        return Mono.just(Result.builder().ckId(id).build());
    }

    @Transactional
    public Mono<Result> delete(DSourceTypeDelete data, String user) {
        var query = queryFactory
                .selectFrom(QTDSourceType.tDSourceType);
        QueryUtil.util.initWhereValue(query, data, QTDSourceType.tDSourceType);
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
                    .delete(QTDSourceType.tDSourceType);
            QueryUtil.util.initWhereValue(queryDelete, data, QTDSourceType.tDSourceType);

            queryDelete.execute();
        } else {
            var queryUpdate = queryFactory
                    .update(QTDSourceType.tDSourceType);
            QueryUtil.util.initWhereValue(queryUpdate, data, QTDSourceType.tDSourceType);
            queryUpdate.set(QTDSourceType.tDSourceType.clDeleted, true);
            queryUpdate.set(QTDSourceType.tDSourceType.ckUser, user);

            queryUpdate.execute();
        }

        return Mono.just(Result.builder().ckId(data.getCkId()).build());
    }
}
