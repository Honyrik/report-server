package ru.tehnobear.essence.receiver.service.admin.dictionary;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.tehnobear.essence.dao.entries.QTDFormat;
import ru.tehnobear.essence.receiver.dto.admin.dictionary.DFormatDelete;
import ru.tehnobear.essence.receiver.dto.admin.dictionary.DFormatFetch;
import ru.tehnobear.essence.receiver.dto.admin.dictionary.DFormatInsert;
import ru.tehnobear.essence.receiver.dto.admin.dictionary.DFormatResult;
import ru.tehnobear.essence.share.dto.Result;
import ru.tehnobear.essence.share.util.QueryUtil;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class DFormatService {
    private final JPAQueryFactory queryFactory;
    private static final String ID_KEY = "ckId";
    private static final String ID_KEY_OLD = "ckIdOld";

    public Mono<DFormatResult> fetch(DFormatFetch fetch) {
        var query = queryFactory.selectFrom(QTDFormat.tDFormat);
        if (!fetch.getData().containsKey("clDeleted")) {
            fetch.getData().put("clDeleted", false);
        }
        QueryUtil.util.filterAndSort(query, fetch, QTDFormat.tDFormat);
        var res = query.fetchResults();

        return Mono.just(
            DFormatResult
                .builder()
                .total(res.getTotal())
                .data(res.getResults())
                .build()
        );
    }

    @Transactional
    public Mono<Result> insert(DFormatInsert data, String user) {
        var query = queryFactory
                .insert(QTDFormat.tDFormat);
        QueryUtil.util.initInsertValue(query, data, QTDFormat.tDFormat);
        query.columns(QTDFormat.tDFormat.ckUser);
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
                .update(QTDFormat.tDFormat);
        QueryUtil.util.initWhereValue(query, Map.of(ID_KEY, id), QTDFormat.tDFormat);
        QueryUtil.util.initSetValue(query, data, QTDFormat.tDFormat);
        query.set(QTDFormat.tDFormat.ckUser, user);

        query.execute();

        return Mono.just(Result.builder().ckId(id).build());
    }

    @Transactional
    public Mono<Result> delete(DFormatDelete data, String user) {
        var query = queryFactory
                .selectFrom(QTDFormat.tDFormat);
        QueryUtil.util.initWhereValue(query, data, QTDFormat.tDFormat);
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
                    .delete(QTDFormat.tDFormat);
            QueryUtil.util.initWhereValue(queryDelete, data, QTDFormat.tDFormat);

            queryDelete.execute();
        } else {
            var queryUpdate = queryFactory
                    .update(QTDFormat.tDFormat);
            QueryUtil.util.initWhereValue(queryUpdate, data, QTDFormat.tDFormat);
            queryUpdate.set(QTDFormat.tDFormat.clDeleted, true);
            queryUpdate.set(QTDFormat.tDFormat.ckUser, user);

            queryUpdate.execute();
        }

        return Mono.just(Result.builder().ckId(data.getCkId()).build());
    }
}
