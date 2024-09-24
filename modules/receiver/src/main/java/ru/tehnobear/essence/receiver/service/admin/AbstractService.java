package ru.tehnobear.essence.receiver.service.admin;

import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.support.TransactionTemplate;
import ru.tehnobear.essence.share.util.QueryUtil;
import ru.tehnobear.essence.share.util.Util;

import java.util.Map;
import java.util.function.Consumer;

@RequiredArgsConstructor
public abstract class AbstractService {
    public static final String ID_KEY = "ckId";
    public static final String ID_KEY_OLD = "ckIdOld";
    public final JPAQueryFactory queryFactory;
    public final EntityManager entityManager;
    public final TransactionTemplate transactionTemplate;

    public <T> void findObject(EntityPathBase<T> entity, Object whereObj, Consumer<T> consumer) {
        var obj = whereObj;
        if (whereObj == null) {
            consumer.accept(null);
            return;
        }
        if (!(whereObj instanceof Map)) {
            obj = (Map<String, Object>) Util.objectMapper.convertValue(whereObj, Map.class);
        }
        var val = ((Map<String, Object>) obj).get(ID_KEY);
        if (val == null) {
            consumer.accept(null);
            return;
        }
        obj = Map.of(ID_KEY, val);

        var findObj = queryFactory.selectFrom(entity);
        QueryUtil.util.initWhereValue(
                findObj,
                obj,
                entity
        );
        var found = findObj.fetchOne();
        consumer.accept(found);
    }
}
