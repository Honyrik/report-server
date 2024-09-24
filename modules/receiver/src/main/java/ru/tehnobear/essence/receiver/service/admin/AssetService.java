package ru.tehnobear.essence.receiver.service.admin;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Mono;
import ru.tehnobear.essence.dao.dto.EAssetType;
import ru.tehnobear.essence.dao.entries.*;
import ru.tehnobear.essence.receiver.dto.admin.*;
import ru.tehnobear.essence.share.dto.Result;
import ru.tehnobear.essence.share.util.QueryUtil;
import ru.tehnobear.essence.share.util.Util;

import java.util.Map;

@Component
@Slf4j
public class AssetService extends AbstractService {
    public AssetService(JPAQueryFactory queryFactory, EntityManager entityManager, TransactionTemplate transactionTemplate) {
        super(queryFactory, entityManager, transactionTemplate);
    }

    public Mono<AssetResult> fetch(AssetFetch fetch) {
        var query = queryFactory.selectFrom(QTAsset.tAsset);
        if (!fetch.getData().containsKey("clDeleted")) {
            fetch.getData().put("clDeleted", false);
        }
        QueryUtil.util.filterAndSort(query, fetch, QTAsset.tAsset);
        var res = query.fetchResults();

        return Mono.just(
            AssetResult
                .builder()
                .total(res.getTotal())
                .data(res.getResults())
                .build()
        );
    }

    public Mono<Result> insert(AssetInsert data, String user) {
        var result = Result
                .builder().build();
        var dataInsert = Util.objectMapper.convertValue(data, TAsset.class);
        dataInsert.setCbAsset(data.getCbAsset());
        dataInsert.setCvAsset(data.getCvAsset());
        dataInsert.setCkUser(user);
        findObject(QTDAssetType.tDAssetType, data.getType(), found -> {
            if (found == null) {
                result.addError("Not found type");
            } else {
                if (found.getCrType() == EAssetType.BINARY && dataInsert.getCbAsset() == null) {
                    result.addError("Not found cbAsset");
                } else if (found.getCrType() == EAssetType.TEXT && dataInsert.getCvAsset() == null) {
                    result.addError("Not found cvAsset");
                }
                dataInsert.setType(found);
            }
        });

        if (result.isError() || result.isWarning()) {
            return Mono.just(result);
        }
        return transactionTemplate.execute(state -> {
            var res = entityManager.merge(dataInsert);
            state.flush();

            return Mono.just(Result.builder().ckId(res.getCkId()).build());
        });
    }

    public Mono<Result> insertUpload(AssetInsertUpload data, String user) {
        var qb = queryFactory.selectFrom(QTDAssetType.tDAssetType);
        QueryUtil.util.initWhereValue(
                qb,
                data.getJson().getType(),
                QTDAssetType.tDAssetType
        );
        var dataD = qb.fetchOne();
        if (dataD == null) {
            return Mono.just(Result.builder().build().addError("Not found type"));
        }
        switch (dataD.getCrType()) {
            case TEXT -> {
                return Util.util.partToString(data.getUpload()).flatMap(val -> {
                    data.getJson().setCvAsset(val);
                    return insert(data.getJson(), user);
                });
            }
            case BINARY -> {
                return Util.util.partToByte(data.getUpload()).flatMap(val -> {
                    data.getJson().setCbAsset(val);
                    return insert(data.getJson(), user);
                });
            }
        }

        return insert(data.getJson(), user);
    }

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
        if (data.containsKey("type")) {
            findObject(QTDAssetType.tDAssetType, data.get("type"), found -> {
                if (found != null) {
                    data.put("type", Map.of(ID_KEY, found.getCkId()));
                } else {
                    result.addError("Not Found type");
                }
            });
        }
        if (result.isError() || result.isWarning()) {
            return Mono.just(result);
        }
        var query = queryFactory
                .update(QTAsset.tAsset);
        QueryUtil.util.initWhereValue(query, Map.of(ID_KEY, id), QTAsset.tAsset);
        QueryUtil.util.initSetValue(query, data, QTAsset.tAsset);
        query.set(QTAsset.tAsset.ckUser, user);


        Object finalId = id;
        return transactionTemplate.execute(state -> {
            query.execute();
            state.flush();

            result.setCkId(finalId);
            return Mono.just(result);
        });
    }

    public Mono<Result> updateUpload(AssetUpdateUpload data, String user) {
        var id = data.getJson().get(ID_KEY_OLD);
        if (id == null) {
            id = data.getJson().get(ID_KEY);
        }
        if (id == null) {
            return Mono.just(
                    Result
                            .builder()
                            .build()
                            .addError("Not Found ID")
            );
        }
        var qb = queryFactory.selectFrom(QTAsset.tAsset);
        QueryUtil.util.initWhereValue(
                qb,
                Map.of(ID_KEY, id),
                QTAsset.tAsset
        );
        var dataD = qb.fetchOne();
        var type = dataD.getType();
        if (data.getJson().containsKey("type")) {
            var findTypeQB = queryFactory.selectFrom(QTDAssetType.tDAssetType);
            QueryUtil.util.initWhereValue(
                    findTypeQB,
                    data.getJson().get("type"),
                    QTDAssetType.tDAssetType
            );
            var updateType = findTypeQB.fetchOne();
            if (updateType != null) {
                type = updateType;
            }
        }

        switch (type.getCrType()) {
            case TEXT -> {
                return Util.util.partToString(data.getUpload()).flatMap(val -> {
                    data.getJson().put("cvAsset", val);
                    return update(data.getJson(), user);
                });
            }
            case BINARY -> {
                return Util.util.partToByte(data.getUpload()).flatMap(val -> {
                    data.getJson().put("cbAsset", val);
                    return update(data.getJson(), user);
                });
            }
        }

        return update(data.getJson(), user);
    }

    public Mono<Result> delete(AssetDelete data, String user) {
        var query = queryFactory
                .selectFrom(QTAsset.tAsset);
        QueryUtil.util.initWhereValue(query, data, QTAsset.tAsset);
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
        return transactionTemplate.execute(state -> {
            if (value.isClDeleted()) {
                var queryDelete = queryFactory
                        .delete(QTAsset.tAsset);
                QueryUtil.util.initWhereValue(queryDelete, data, QTAsset.tAsset);

                queryDelete.execute();
            } else {
                var queryUpdate = queryFactory
                        .update(QTAsset.tAsset);
                QueryUtil.util.initWhereValue(queryUpdate, data, QTAsset.tAsset);
                queryUpdate.set(QTAsset.tAsset.clDeleted, true);
                queryUpdate.set(QTAsset.tAsset.ckUser, user);

                queryUpdate.execute();
            }
            state.flush();

            return Mono.just(Result.builder().ckId(data.getCkId()).build());
        });
    }
}
