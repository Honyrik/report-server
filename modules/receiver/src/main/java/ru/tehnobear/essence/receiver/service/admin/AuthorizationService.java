package ru.tehnobear.essence.receiver.service.admin;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.tehnobear.essence.dao.entries.QTAuthorization;
import ru.tehnobear.essence.dao.entries.TAuthorization;
import ru.tehnobear.essence.receiver.dto.admin.AuthorizationDelete;
import ru.tehnobear.essence.receiver.dto.admin.AuthorizationFetch;
import ru.tehnobear.essence.receiver.dto.admin.AuthorizationInsert;
import ru.tehnobear.essence.receiver.dto.admin.AuthorizationResult;
import ru.tehnobear.essence.share.dto.Result;
import ru.tehnobear.essence.share.util.QueryUtil;
import ru.tehnobear.essence.share.util.Util;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuthorizationService {
    private final JPAQueryFactory queryFactory;
    private final EntityManager entityManager;
    private static final String ID_KEY = "ckId";
    private static final String ID_KEY_OLD = "ckIdOld";

    public Mono<AuthorizationResult> fetch(AuthorizationFetch fetch) {
        var query = queryFactory.selectFrom(QTAuthorization.tAuthorization);
        if (!fetch.getData().containsKey("clDeleted")) {
            fetch.getData().put("clDeleted", false);
        }
        QueryUtil.util.filterAndSort(query, fetch, QTAuthorization.tAuthorization);
        var res = query.fetchResults();

        return Mono.just(
            AuthorizationResult
                .builder()
                .total(res.getTotal())
                .data(res.getResults())
                .build()
        );
    }

    @Transactional
    public Mono<Result> insert(AuthorizationInsert data, String user) {
        var dataInsert = Util.objectMapper.convertValue(data, TAuthorization.class);
        dataInsert.setCkUser(user);
        var res = entityManager.merge(dataInsert);

        return Mono.just(Result.builder().ckId(res.getCkId()).build());
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
                .update(QTAuthorization.tAuthorization);
        QueryUtil.util.initWhereValue(query, Map.of(ID_KEY, id), QTAuthorization.tAuthorization);
        QueryUtil.util.initSetValue(query, data, QTAuthorization.tAuthorization);
        query.set(QTAuthorization.tAuthorization.ckUser, user);

        query.execute();

        return Mono.just(Result.builder().ckId(id).build());
    }

    @Transactional
    public Mono<Result> delete(AuthorizationDelete data, String user) {
        var query = queryFactory
                .selectFrom(QTAuthorization.tAuthorization);
        QueryUtil.util.initWhereValue(query, data, QTAuthorization.tAuthorization);
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
                    .delete(QTAuthorization.tAuthorization);
            QueryUtil.util.initWhereValue(queryDelete, data, QTAuthorization.tAuthorization);

            queryDelete.execute();
        } else {
            var queryUpdate = queryFactory
                    .update(QTAuthorization.tAuthorization);
            QueryUtil.util.initWhereValue(queryUpdate, data, QTAuthorization.tAuthorization);
            queryUpdate.set(QTAuthorization.tAuthorization.clDeleted, true);
            queryUpdate.set(QTAuthorization.tAuthorization.ckUser, user);

            queryUpdate.execute();
        }

        return Mono.just(Result.builder().ckId(data.getCkId()).build());
    }
}
