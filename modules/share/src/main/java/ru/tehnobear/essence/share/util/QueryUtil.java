package ru.tehnobear.essence.share.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.querydsl.core.FilteredClause;
import com.querydsl.core.dml.InsertClause;
import com.querydsl.core.dml.StoreClause;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.impl.JPAQuery;
import lombok.extern.slf4j.Slf4j;
import ru.tehnobear.essence.share.dto.ESort;
import ru.tehnobear.essence.share.dto.Fetch;
import ru.tehnobear.essence.share.dto.Filter;

import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
public class QueryUtil {
    public static QueryUtil util = new QueryUtil();

    public String writeValueAsStringSilentAll(Object obj) {
        try {
            return Util.objectMapperAll.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error(e.getOriginalMessage(), e);
            return "";
        }
    }
    public String writeValueAsStringSilent(Object obj) {
        try {
            return Util.objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error(e.getOriginalMessage(), e);
            return "";
        }
    }
    public void filterAndSort(JPAQuery query, Fetch fetch, EntityPathBase entity) {
        query.limit(fetch.getFetch() == null ? 2000 : fetch.getFetch())
                .offset(fetch.getOffset() == null ? 0 : fetch.getOffset());
        if (fetch.getSort() != null && !fetch.getSort().isEmpty()) {
            fetch.getSort().stream().forEach(order -> {
                if (!order.getProperty().trim().isEmpty()) {
                    nestedSort(query, order.getDirection(), Arrays.asList(order.getProperty().split("\\.")), entity);
                }
            });
        }
        if (fetch.getData() != null) {
            BooleanExpression res = null;
            for (var obj : fetch.getData().entrySet()) {
                BooleanExpression exp = nestedData(obj.getValue(), obj.getKey(), entity, true);
                if (exp != null) {
                    res = res == null ? exp : res.and(exp);
                }
            }
            if (res != null) {
                query.where(res);
            }
        }
        if (fetch.getFilter() != null && !fetch.getFilter().isEmpty()) {
            BooleanExpression res = null;
            for (var filter : fetch.getFilter()) {
                BooleanExpression exp = nestedFilter(filter, Arrays.asList(filter.getProperty().split("\\.")), entity);
                if (exp != null) {
                    res = res == null ? exp : res.and(exp);
                }
            }
            if (res != null) {
                query.where(res);
            }
        }
    }

    private void nestedSort(JPAQuery query, ESort direction, List<String> property, EntityPathBase entity) {
        var firstProperty = property.stream().findFirst();
        if (firstProperty.isEmpty()) {
            return;
        }
        var fields = Arrays.asList(entity.getClass().getDeclaredFields());
        var fieldOpt = fields.stream()
                .filter(field -> field.getName().equalsIgnoreCase(firstProperty.get()))
                .findFirst();
        if (fieldOpt.isPresent()) {
            try {
                var extVal = fieldOpt.get().get(entity);
                if (extVal instanceof EntityPathBase) {
                    nestedSort(query, direction, property.subList(1, property.size()), (EntityPathBase<?>) extVal);
                    return;
                }
                if (!(extVal instanceof ComparableExpression)) {
                    return;
                }
                query.orderBy(direction == ESort.DESC ?
                        ((ComparableExpression<?>) extVal).desc() :
                        ((ComparableExpression<?>) extVal).asc());
            } catch (IllegalAccessException e) {
                log.warn("Error sort property {}", firstProperty.get(), e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public Object parseValue(DslExpression exp, Object value) {
        if (value == null) {
            return value;
        }
        if (value instanceof Collection) {
            return ((Collection) value).stream().map(val -> parseValue(exp, val)).toList();
        }
        if (value.getClass().isArray()) {
            return Arrays.stream((Object[]) value).map(val -> parseValue(exp, val)).toList();
        }
        if (exp.getType().isEnum() && value instanceof String) {
            try {
                var method = exp.getType().getMethod("values");
                var arr = (Enum[]) method.invoke(null);
                return Arrays.stream(arr)
                        .filter(val -> val.name().equalsIgnoreCase((String) value))
                        .findFirst()
                        .orElse(null);
            } catch (NoSuchMethodException e) {
                log.debug("Error parse", e);
            } catch (InvocationTargetException e) {
                log.debug("Error parse", e);
            } catch (IllegalAccessException e) {
                log.debug("Error parse", e);
            }
        }
        if (exp.getType() == UUID.class && value instanceof String) {
            try {
                return UUID.fromString((String) value);
            } catch (IllegalArgumentException e) {
                log.debug("Error parse", e);
            }
        }
        if (exp.getType() == Duration.class && value instanceof String) {
            try {
                return Duration.parse((String) value);
            } catch (IllegalArgumentException e) {
                log.debug("Error parse", e);
            }
        }
        if (exp.getType() == Instant.class && value instanceof String) {
            try {
                return Instant.parse((String) value);
            } catch (Exception e) {
                log.debug("Error parse", e);
            }
            try {
                return DateTimeFormatter.ISO_LOCAL_DATE_TIME.parse((String) value, Instant::from);
            } catch (Exception e) {
                log.debug("Error parse", e);
            }
            try {
                return DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse((String) value, Instant::from);
            } catch (Exception e) {
                log.debug("Error parse", e);
            }
        }
        if (exp.getType() == Boolean.class) {
            if (value instanceof String && !((String) value).trim().isEmpty()) {
                return switch (((String) value).toLowerCase()) {
                    case "y", "yes", "on", "1", "true" -> true;
                    default -> false;
                };
            }
            if (value instanceof Number) {
                return ((Number) value).longValue() == 0 ? false : true;
            }
        }
        return value;
    }

    private BooleanExpression nestedFilter(Filter filter, List<String> property, EntityPathBase entity) {
        var firstProperty = property.stream().findFirst();
        if (firstProperty.isEmpty()) {
            return null;
        }
        var fields = Arrays.asList(entity.getClass().getDeclaredFields());
        var fieldOpt = fields.stream()
                .filter(field -> field.getName().equalsIgnoreCase(firstProperty.get()))
                .findFirst();
        if (fieldOpt.isPresent()) {
            try {
                var expValObj = fieldOpt.get().get(entity);
                var propertys = property.subList(1, property.size());
                if (expValObj instanceof EntityPathBase && !propertys.isEmpty()) {
                    return nestedFilter(filter, propertys, (EntityPathBase<?>) expValObj);
                }
                if (expValObj instanceof EntityPathBase) {
                    switch (filter.getOperator()) {
                        case EQ -> {
                            if (filter.getValue() instanceof Map) {
                                BooleanExpression res = null;
                                for (var val : ((Map<String, Object>) filter.getValue()).entrySet()) {
                                    var resChild = nestedData(val.getValue(), val.getKey(), (EntityPathBase<?>) expValObj, true);
                                    if (resChild != null) {
                                        res = res == null ? resChild : res.and(resChild);
                                    }
                                }
                                return res;
                            }
                            return ((EntityPathBase<?>) expValObj).isNull();
                        }
                        case NE -> {
                            if (filter.getValue() instanceof Map) {
                                BooleanExpression res = null;
                                for (var val : ((Map<String, Object>) filter.getValue()).entrySet()) {
                                    var resChild = nestedData(val.getValue(), val.getKey(), (EntityPathBase<?>) expValObj, false);
                                    if (resChild != null) {
                                        res = res == null ? resChild : res.and(resChild);
                                    }
                                }
                                return res;
                            }
                            return ((EntityPathBase<?>) expValObj).isNotNull();
                        }
                        case NOTNULL -> {
                            return ((EntityPathBase<?>) expValObj).isNotNull();
                        }
                        case NULL -> {
                            return ((EntityPathBase<?>) expValObj).isNull();
                        }
                        default -> {
                            return null;
                        }
                    }
                }
                if (!(expValObj instanceof ComparableExpression)) {
                    return null;
                }
                var exp = (ComparableExpression) expValObj;
                var val = parseValue(exp, filter.getValue());
                switch (filter.getOperator()) {
                    case GT -> {
                        return exp.gt((Comparable) val);
                    }
                    case GE -> {
                        return exp.gt((Comparable) val).or(exp.eq(val));
                    }
                    case LT -> {
                        return exp.lt((Comparable) val);
                    }
                    case LE -> {
                        return exp.lt((Comparable) val).or(exp.eq(val));
                    }
                    case EQ -> {
                        if (val instanceof Collection) {
                            return exp.in((Collection) val);
                        }
                        return val == null ? exp.isNull() : exp.eq(val);
                    }
                    case LIKE -> {
                        return Expressions.stringOperation(Ops.STRING_CAST, exp).likeIgnoreCase(String.format("%%%s%%", filter.getValue()));
                    }
                    case NOTLIKE -> {
                        return Expressions.stringOperation(Ops.STRING_CAST, exp).toUpperCase().notLike(String.format("%%%s%%", filter.getValue()).toUpperCase());
                    }
                    case IN -> {
                        return exp.in((Collection) val);
                    }
                    case NOTIN -> {
                        return exp.notIn((Collection) val);
                    }
                    case NE -> {
                        if (val instanceof Collection) {
                            return exp.notIn((Collection) val);
                        }
                        return val == null ? exp.isNotNull() : exp.ne(val);
                    }
                    case NOTNULL -> {
                        return exp.isNotNull();
                    }
                    case NULL -> {
                        return exp.isNull();
                    }
                }
            } catch (IllegalAccessException e) {
                log.warn("Error sort property {}", firstProperty.get(), e);
            }
        }

        return null;
    }

    private BooleanExpression nestedData(Object value, String property, EntityPathBase entity, Boolean isEqual) {
        var fields = Arrays.asList(entity.getClass().getDeclaredFields());
        var fieldOpt = fields.stream()
                .filter(field -> field.getName().equalsIgnoreCase(property))
                .findFirst();
        if (fieldOpt.isPresent()) {
            try {
                var expValObj = fieldOpt.get().get(entity);
                if (expValObj instanceof EntityPathBase && value instanceof Map) {
                    BooleanExpression res = null;
                    for (var obj : ((Map<String , Object>) value).entrySet()) {
                        BooleanExpression exp = nestedData(obj.getValue(), obj.getKey(), (EntityPathBase) expValObj, isEqual);
                        if (exp != null) {
                            res = res == null ? exp : res.and(exp);
                        }
                    }
                    if (res != null) {
                        return res;
                    }
                }
                if (expValObj instanceof EntityPathBase && value == null) {
                    return ((EntityPathBase<?>) expValObj).isNull();
                }
                if (!(expValObj instanceof SimpleExpression)) {
                    return null;
                }
                var exp = (SimpleExpression) expValObj;
                var val = parseValue(exp, value);
                if (value instanceof Collection) {
                    return isEqual ? exp.in(val) : exp.notIn(val);
                }
                if (val == null) {
                    return isEqual ? exp.isNull() : exp.isNotNull();
                }
                return isEqual ? exp.eq(val) : exp.ne(val);
            } catch (IllegalAccessException e) {
                log.warn("Error sort property {}", property, e);
            }
        }

        return null;
    }

    public void initSetValue(StoreClause query, Object value, EntityPathBase entity) {
        if (value instanceof Map) {
            setValue(query, (Map<String, Object>) value, entity, 0);
            return;
        }
        setValue(query, (Map<String, Object>) Util.objectMapper.convertValue(value, Map.class), entity, 0);
    }

    private void setValue(StoreClause query, Map<String, Object> value, EntityPathBase entity, Integer lvl) {
        var fields = Arrays.asList(entity.getClass().getDeclaredFields());
        for(var entityObj : value.entrySet()) {
            var fieldOpt = fields.stream()
                    .filter(field -> field.getName().equalsIgnoreCase(entityObj.getKey()))
                    .findFirst();
            if (fieldOpt.isPresent()) {
                try {
                    var expValObj = fieldOpt.get().get(entity);
                    if (expValObj instanceof EntityPathBase && entityObj.getValue() instanceof Map) {
                        setValue(query, (Map<String, Object>) entityObj.getValue(), (EntityPathBase) expValObj, lvl+1);
                        continue;
                    }
                    if (expValObj instanceof EntityPathBase) {
                        query.set((Path) expValObj, entityObj.getValue());
                        continue;
                    }
                    if (!(expValObj instanceof Path)) {
                        log.error("Error sort property {}", entityObj.getKey());
                        continue;
                    }
                    var exp = (DslExpression) expValObj;
                    var val = parseValue(exp, entityObj.getValue());
                    query.set((Path) expValObj, val);
                } catch (IllegalAccessException e) {
                    log.error("Error sort property {}", entityObj.getKey(), e);
                }
            }
        }
        if (lvl == 0) {
            var fieldOptChange = fields.stream()
                    .filter(field -> field.getName().equalsIgnoreCase("ctChange"))
                    .findFirst();
            if (fieldOptChange.isPresent()) {
                try {
                    var expValObj = fieldOptChange.get().get(entity);
                    var exp = (DslExpression) expValObj;
                    var val = parseValue(exp, Instant.now());
                    query.set((Path) expValObj, val);
                } catch (IllegalAccessException e) {
                    log.error("Error sort property ctChange", e);
                }
            }
        }
    }

    public void initInsertValue(InsertClause query, Object value, EntityPathBase entity) {
        if (value instanceof Map) {
            insertValue(query, (Map<String, Object>) value, entity, 0);
            return;
        }
        insertValue(query, (Map<String, Object>) Util.objectMapper.convertValue(value, Map.class), entity, 0);
    }

    private void insertValue(InsertClause query, Map<String, Object> value, EntityPathBase entity, int lvl) {
        var fields = Arrays.asList(entity.getClass().getDeclaredFields());
        for(var entityObj : value.entrySet()) {
            var fieldOpt = fields.stream()
                    .filter(field -> field.getName().equalsIgnoreCase(entityObj.getKey()))
                    .findFirst();
            if (fieldOpt.isPresent()) {
                try {
                    var expValObj = fieldOpt.get().get(entity);
                    if (!(expValObj instanceof Path)) {
                        log.error("Error sort property {}", entityObj.getKey());
                        continue;
                    }
                    if (!(expValObj instanceof SimpleExpression)) {
                        log.error("Error sort property {}", entityObj.getKey());
                        continue;
                    }
                    var exp = (DslExpression) expValObj;
                    var val = parseValue(exp, entityObj.getValue());
                    query.columns((Path) expValObj);
                    query.values(val);
                } catch (IllegalAccessException e) {
                    log.error("Error sort property {}", entityObj.getKey(), e);
                }
            }
        }
        if (lvl == 0) {
            var now = Instant.now();
            var fieldOptCreate = fields.stream()
                    .filter(field -> field.getName().equalsIgnoreCase("ctCreate"))
                    .findFirst();
            if (fieldOptCreate.isPresent()) {
                try {
                    var expValObj = fieldOptCreate.get().get(entity);
                    var exp = (DslExpression) expValObj;
                    var val = parseValue(exp, now);
                    query.columns((Path) expValObj);
                    query.values(val);
                } catch (IllegalAccessException e) {
                    log.error("Error sort property ctCreate", e);
                }
            }
            var fieldOptChange = fields.stream()
                    .filter(field -> field.getName().equalsIgnoreCase("ctChange"))
                    .findFirst();
            if (fieldOptChange.isPresent()) {
                try {
                    var expValObj = fieldOptChange.get().get(entity);
                    var exp = (DslExpression) expValObj;
                    var val = parseValue(exp, now);
                    query.columns((Path) expValObj);
                    query.values(val);
                } catch (IllegalAccessException e) {
                    log.error("Error sort property ctChange", e);
                }
            }
        }
    }

    public void initWhereValue(FilteredClause query, Object value, EntityPathBase entity) {
        if (value instanceof Map) {
            whereValue(query, (Map<String, Object>) value, entity);
            return;
        }
        whereValue(query, (Map<String, Object>) Util.objectMapper.convertValue(value, Map.class), entity);
    }

    private void whereValue(FilteredClause query, Map<String, Object> value, EntityPathBase entity) {
        var fields = Arrays.asList(entity.getClass().getDeclaredFields());
        for(var entityObj : value.entrySet()) {
            var fieldOpt = fields.stream()
                    .filter(field -> field.getName().equalsIgnoreCase(entityObj.getKey()))
                    .findFirst();
            if (fieldOpt.isPresent()) {
                try {
                    var expValObj = fieldOpt.get().get(entity);
                    if (expValObj instanceof EntityPathBase && entityObj.getValue() instanceof Map) {
                        whereValue(query, (Map<String, Object>) entityObj.getValue(), (EntityPathBase) expValObj);
                        continue;
                    }
                    if (!(expValObj instanceof SimpleExpression)) {
                        log.error("Error sort property {}", entityObj.getKey());
                        continue;
                    }
                    var exp = (SimpleExpression) expValObj;
                    var val = parseValue(exp, entityObj.getValue());
                    if (val != null)
                        query.where(exp.eq(val));
                } catch (IllegalAccessException e) {
                    log.error("Error sort property {}", entityObj.getKey(), e);
                }
            }
        }
    }

}
