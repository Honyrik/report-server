package ru.tehnobear.essence.receiver.service.admin.dictionary;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.tehnobear.essence.dao.entries.QTDAssetType;
import ru.tehnobear.essence.receiver.dto.admin.dictionary.DAssetTypeDelete;
import ru.tehnobear.essence.receiver.dto.admin.dictionary.DAssetTypeFetch;
import ru.tehnobear.essence.receiver.dto.admin.dictionary.DAssetTypeInsert;
import ru.tehnobear.essence.receiver.dto.admin.dictionary.DAssetTypeResult;
import ru.tehnobear.essence.share.dto.Result;
import ru.tehnobear.essence.share.util.QueryUtil;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class DAssetTypeService {
    private final JPAQueryFactory queryFactory;
    private static final String ID_KEY = "ckId";
    private static final String ID_KEY_OLD = "ckIdOld";

    public Mono<DAssetTypeResult> fetch(DAssetTypeFetch fetch) {
        var query = queryFactory.selectFrom(QTDAssetType.tDAssetType);
        if (!fetch.getData().containsKey("clDeleted")) {
            fetch.getData().put("clDeleted", false);
        }
        QueryUtil.util.filterAndSort(query, fetch, QTDAssetType.tDAssetType);
        var res = query.fetchResults();

        return Mono.just(
            DAssetTypeResult
                .builder()
                .total(res.getTotal())
                .data(res.getResults())
                .build()
        );
    }

    @Transactional
    public Mono<Result> insert(DAssetTypeInsert data, String user) {
        var query = queryFactory
                .insert(QTDAssetType.tDAssetType);

        QueryUtil.util.initInsertValue(query, data, QTDAssetType.tDAssetType);
        query.columns(QTDAssetType.tDAssetType.ckUser);
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
                .update(QTDAssetType.tDAssetType);
        QueryUtil.util.initWhereValue(query, Map.of(ID_KEY, id), QTDAssetType.tDAssetType);
        QueryUtil.util.initSetValue(query, data, QTDAssetType.tDAssetType);
        query.set(QTDAssetType.tDAssetType.ckUser, user);

        query.execute();

        return Mono.just(Result.builder().ckId(id).build());
    }

    @Transactional
    public Mono<Result> delete(DAssetTypeDelete data, String user) {
        var query = queryFactory
                .selectFrom(QTDAssetType.tDAssetType);
        QueryUtil.util.initWhereValue(query, data, QTDAssetType.tDAssetType);
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
                    .delete(QTDAssetType.tDAssetType);
            QueryUtil.util.initWhereValue(queryDelete, data, QTDAssetType.tDAssetType);

            queryDelete.execute();
        } else {
            var queryUpdate = queryFactory
                    .update(QTDAssetType.tDAssetType);
            QueryUtil.util.initWhereValue(queryUpdate, data, QTDAssetType.tDAssetType);
            queryUpdate.set(QTDAssetType.tDAssetType.clDeleted, true);
            queryUpdate.set(QTDAssetType.tDAssetType.ckUser, user);

            queryUpdate.execute();
        }

        return Mono.just(Result.builder().ckId(data.getCkId()).build());
    }
}
