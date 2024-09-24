package ru.tehnobear.essence.receiver.service.admin;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Mono;
import ru.tehnobear.essence.dao.dto.EStatus;
import ru.tehnobear.essence.dao.entries.*;
import ru.tehnobear.essence.receiver.dto.admin.QueueDelete;
import ru.tehnobear.essence.receiver.dto.admin.QueueFetch;
import ru.tehnobear.essence.receiver.dto.admin.QueueInsert;
import ru.tehnobear.essence.receiver.dto.admin.QueueResult;
import ru.tehnobear.essence.share.dto.Result;
import ru.tehnobear.essence.share.util.QueryUtil;
import ru.tehnobear.essence.share.util.Util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class QueueService extends AbstractService {
    public QueueService(JPAQueryFactory queryFactory, EntityManager entityManager, TransactionTemplate transactionTemplate) {
        super(queryFactory, entityManager, transactionTemplate);
    }

    public Mono<QueueResult> fetch(QueueFetch fetch) {
        var query = queryFactory.selectFrom(QTQueue.tQueue);
        if (!fetch.getData().containsKey("clDeleted")) {
            fetch.getData().put("clDeleted", false);
        }
        QueryUtil.util.filterAndSort(query, fetch, QTQueue.tQueue);
        var res = query.fetchResults();

        return Mono.just(
            QueueResult
                .builder()
                .total(res.getTotal())
                .data(res.getResults())
                .build()
        );
    }

    @Transactional
    public Mono<Result> insert(QueueInsert data, String user) {
        var result = Result
                .builder()
                .build();
        var dataInsert = Util.objectMapper.convertValue(data, TQueue.class);
        dataInsert.setCkId(UUID.randomUUID());
        dataInsert.setCkUser(user);
        findObject(QTReport.tReport, data.getReport(), found -> {
            if (found != null && !found.isClDeleted()) {
                dataInsert.setReport(found);
                if (data.getCnPriority() == null) {
                    dataInsert.setCnPriority(found.getCnPriority());
                }
            } else {
                result.addError("Not Found report");
            }
        });
        findObject(QTReportFormat.tReportFormat, data.getFormat(), found -> {
            if (found != null && found.getReport().getCkId().equals(dataInsert.getReport().getCkId())) {
                dataInsert.setFormat(found);
            } else {
                result.addError("Not Found format");
            }
        });
        findObject(
                QTDQueue.tDQueue,
                data.getQueue(),
                found -> {
                    if (found != null) {
                        dataInsert.setQueue(found);
                    } else if(!result.isError() && !result.isWarning()) {
                        dataInsert.setQueue(dataInsert.getReport().getQueue());
                    } else {
                        result.addError("Not Found queue");
                    }
                }
        );
        findObject(QTDStatus.tDStatus, data.getStatus(), found -> {
            if (found != null) {
                dataInsert.setStatus(found);
            } else {
                findObject(QTDStatus.tDStatus, Map.of(ID_KEY, EStatus.NEW), foundNew -> {
                    if (foundNew != null) {
                        dataInsert.setStatus(foundNew);
                    } else {
                        result.addError("Not Found status");
                    }
                });
            }
        });
        if (data.getScheduler() != null) {
            findObject(QTScheduler.tScheduler, data.getScheduler(), dataInsert::setScheduler);
        }
        if (result.isError() || result.isWarning()) {
            return Mono.just(result);
        }

        var res = entityManager.merge(dataInsert);
        result.setCkId(res.getCkId());
        return Mono.just(result);
    }

    @Transactional
    public Mono<Result> update(Map<String, Object> data, String user) {
        var result = Result
                .builder()
                .build();
        var id = data.remove(ID_KEY_OLD);
        if (id == null) {
            id = data.remove(ID_KEY);
        }
        if (id == null) {
            result.addError("Not Found ID");
        }

        if (data.containsKey("report")) {
            findObject(QTReport.tReport, data.get("report"), found -> {
                if (found != null && !found.isClDeleted()) {
                    data.put("report", Map.of(ID_KEY, found.getCkId()));
                } else {
                    result.addError("Not Found report");
                }
            });
        }

        if (data.containsKey("format")) {
            findObject(QTReportFormat.tReportFormat, data.get("format"), found -> {
                if (found != null) {
                    data.put("format", Map.of(ID_KEY, found.getCkId()));
                } else {
                    result.addError("Not Found format");
                }
            });
        }
        if (data.containsKey("queue")) {
            findObject(QTDQueue.tDQueue, data.get("queue"), found -> {
                if (found != null) {
                    data.put("queue", Map.of(ID_KEY, found.getCkId()));
                } else {
                    result.addError("Not Found queue");
                }
            });
        }

        if (data.containsKey("status")) {
            findObject(QTDStatus.tDStatus, data.get("status"), found -> {
                if (found != null) {
                    data.put("status", Map.of(ID_KEY, found.getCkId()));
                } else {
                    result.addError("Not Found status");
                }
            });
        }
        if (data.containsKey("scheduler")) {
            findObject(QTScheduler.tScheduler, data.get("scheduler"), found -> {
                if (found != null) {
                    data.put("scheduler", Map.of(ID_KEY, found.getCkId()));
                } else {
                    var map = new HashMap<>();
                    map.put(ID_KEY, null);
                    data.put("scheduler", map);
                }
            });
        }

        if (result.isError() || result.isWarning()) {
            return Mono.just(result);
        }
        var query = queryFactory
                .update(QTQueue.tQueue);
        QueryUtil.util.initWhereValue(query, Map.of(ID_KEY, id), QTQueue.tQueue);
        QueryUtil.util.initSetValue(query, data, QTQueue.tQueue);
        query.set(QTQueue.tQueue.ckUser, user);
        query.execute();

        result.setCkId(id);
        return Mono.just(result);
    }

    @Transactional
    public Mono<Result> delete(QueueDelete data, String user) {
        var query = queryFactory
                .selectFrom(QTQueue.tQueue);
        QueryUtil.util.initWhereValue(query, data, QTQueue.tQueue);
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
            var deleteLog = queryFactory
                    .delete(QTQueueLog.tQueueLog);
            QueryUtil.util.initWhereValue(
                    deleteLog, Map.of("queue", Map.of(ID_KEY, data.getCkId())), QTQueueLog.tQueueLog);
            deleteLog.execute();

            var deleteStorage = queryFactory
                    .delete(QTQueueStorage.tQueueStorage);
            QueryUtil.util.initWhereValue(
                    deleteStorage, Map.of("queue", Map.of(ID_KEY, data.getCkId())), QTQueueStorage.tQueueStorage);
            deleteStorage.execute();

            var queryDelete = queryFactory
                    .delete(QTQueue.tQueue);
            QueryUtil.util.initWhereValue(queryDelete, data, QTQueue.tQueue);

            queryDelete.execute();
        } else {
            var queryUpdate = queryFactory
                    .update(QTQueue.tQueue);
            QueryUtil.util.initWhereValue(queryUpdate, data, QTQueue.tQueue);
            queryUpdate.set(QTQueue.tQueue.clDeleted, true);
            queryUpdate.set(QTQueue.tQueue.ckUser, user);

            queryUpdate.execute();
        }

        return Mono.just(Result.builder().ckId(data.getCkId()).build());
    }
}
