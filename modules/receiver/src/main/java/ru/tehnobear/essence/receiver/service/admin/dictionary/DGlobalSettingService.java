package ru.tehnobear.essence.receiver.service.admin.dictionary;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.tehnobear.essence.dao.entries.QTDGlobalSetting;
import ru.tehnobear.essence.receiver.dto.admin.dictionary.DGlobalSettingDelete;
import ru.tehnobear.essence.receiver.dto.admin.dictionary.DGlobalSettingFetch;
import ru.tehnobear.essence.receiver.dto.admin.dictionary.DGlobalSettingInsert;
import ru.tehnobear.essence.receiver.dto.admin.dictionary.DGlobalSettingResult;
import ru.tehnobear.essence.share.dto.Result;
import ru.tehnobear.essence.share.util.QueryUtil;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class DGlobalSettingService {
    private final JPAQueryFactory queryFactory;
    private static final String ID_KEY = "ckId";
    private static final String ID_KEY_OLD = "ckIdOld";

    public Mono<DGlobalSettingResult> fetch(DGlobalSettingFetch fetch) {
        var query = queryFactory.selectFrom(QTDGlobalSetting.tDGlobalSetting);
        if (!fetch.getData().containsKey("clDeleted")) {
            fetch.getData().put("clDeleted", false);
        }
        QueryUtil.util.filterAndSort(query, fetch, QTDGlobalSetting.tDGlobalSetting);
        var res = query.fetchResults();

        return Mono.just(
            DGlobalSettingResult
                .builder()
                .total(res.getTotal())
                .data(res.getResults())
                .build()
        );
    }

    @Transactional
    public Mono<Result> insert(DGlobalSettingInsert data, String user) {
        var query = queryFactory
                .insert(QTDGlobalSetting.tDGlobalSetting);
        QueryUtil.util.initInsertValue(query, data, QTDGlobalSetting.tDGlobalSetting);
        query.columns(QTDGlobalSetting.tDGlobalSetting.ckUser);
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
                .update(QTDGlobalSetting.tDGlobalSetting);
        QueryUtil.util.initWhereValue(query, Map.of(ID_KEY, id), QTDGlobalSetting.tDGlobalSetting);
        QueryUtil.util.initSetValue(query, data, QTDGlobalSetting.tDGlobalSetting);
        query.set(QTDGlobalSetting.tDGlobalSetting.ckUser, user);

        query.execute();

        return Mono.just(Result.builder().ckId(id).build());
    }

    @Transactional
    public Mono<Result> delete(DGlobalSettingDelete data, String user) {
        var query = queryFactory
                .selectFrom(QTDGlobalSetting.tDGlobalSetting);
        QueryUtil.util.initWhereValue(query, data, QTDGlobalSetting.tDGlobalSetting);
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
                    .delete(QTDGlobalSetting.tDGlobalSetting);
            QueryUtil.util.initWhereValue(queryDelete, data, QTDGlobalSetting.tDGlobalSetting);

            queryDelete.execute();
        } else {
            var queryUpdate = queryFactory
                    .update(QTDGlobalSetting.tDGlobalSetting);
            QueryUtil.util.initWhereValue(queryUpdate, data, QTDGlobalSetting.tDGlobalSetting);
            queryUpdate.set(QTDGlobalSetting.tDGlobalSetting.clDeleted, true);
            queryUpdate.set(QTDGlobalSetting.tDGlobalSetting.ckUser, user);

            queryUpdate.execute();
        }

        return Mono.just(Result.builder().ckId(data.getCkId()).build());
    }
}
