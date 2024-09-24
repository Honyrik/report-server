package ru.tehnobear.essence.receiver.service;

import com.querydsl.core.types.Ops;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.tehnobear.essence.dao.dto.EStatus;
import ru.tehnobear.essence.dao.entries.*;
import ru.tehnobear.essence.receiver.dto.admin.PublicReportBody;
import ru.tehnobear.essence.receiver.manager.AuthorizationManager;
import ru.tehnobear.essence.receiver.service.admin.AbstractService;
import ru.tehnobear.essence.share.dto.EMessage;
import ru.tehnobear.essence.share.dto.Result;
import ru.tehnobear.essence.share.manager.ReportRunnerManager;
import ru.tehnobear.essence.share.manager.StorageManager;

import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class PublicReportService extends AbstractService {
    private final StorageManager storageManager;
    private final ReportRunnerManager reportRunnerManager;
    private final AuthorizationManager authorizationManager;
    public PublicReportService(
            JPAQueryFactory queryFactory,
            EntityManager entityManager,
            TransactionTemplate transactionTemplate,
            StorageManager storageManager,
            ReportRunnerManager reportRunnerManager,
            AuthorizationManager authorizationManager
    ) {
        super(queryFactory, entityManager, transactionTemplate);
        this.storageManager = storageManager;
        this.reportRunnerManager = reportRunnerManager;
        this.authorizationManager = authorizationManager;
    }

    public Mono<ResponseEntity<Object>> run(ServerWebExchange exchange, PublicReportBody data, String user){
        var result = Result
                .builder()
                .build();
        var report = queryFactory.selectFrom(QTReport.tReport)
            .where(QTReport.tReport.ckId.eq(data.getReportId()))
            .fetchOne();
        if (report == null) {
            return Mono.just(
                ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(result.addError("Not Found report"))
            );
        }
        return authorizationManager
                .getAuthorization(report.getAuthorization())
                .authorization(exchange, user)
                .flatMap(val -> run(data, user));
    }

    public Mono<ResponseEntity<Object>> run(PublicReportBody data, String user) {
        var result = Result
                .builder()
                .build();
        var dataInsert = new TQueue();
        dataInsert.setCkId(UUID.randomUUID());
        dataInsert.setCkUser(data.getUser() != null && !user.equalsIgnoreCase("99999") ? data.getUser() : user);
        dataInsert.setClOnline(data.isOnline());
        dataInsert.setCctParameter(data.getParameter());
        dataInsert.setCvReportName(data.getReportName());
        if (data.getCleanDate() != null){
            dataInsert.setCtCleaning(data.getCleanDate());
        }
        findObject(QTReport.tReport, Map.of(ID_KEY, data.getReportId()), found -> {
            if (found != null && !found.isClDeleted()) {
                dataInsert.setReport(found);
                if (dataInsert.getCnPriority() == null) {
                    dataInsert.setCnPriority(found.getCnPriority());
                }
                dataInsert.setQueue(dataInsert.getReport().getQueue());
            } else {
                result.addError("Not Found report");
            }
        });

        findObject(
            QTReportFormat.tReportFormat,
            queryFactory.selectFrom(QTReportFormat.tReportFormat)
                .where(
                    QTReportFormat.tReportFormat.format.ckId.eq(data.getFormat())
                    .or(Expressions.stringOperation(Ops.STRING_CAST, QTReportFormat.tReportFormat.ckId).eq(data.getFormat()))
                ).fetchOne(),
            found -> {
            if (found != null && found.getReport().getCkId().equals(dataInsert.getReport().getCkId())) {
                dataInsert.setFormat(found);
            } else {
                result.addError("Not Found format");
            }
        });
        findObject(QTDStatus.tDStatus, Map.of(ID_KEY, EStatus.NEW), foundNew -> {
            if (foundNew != null) {
                dataInsert.setStatus(foundNew);
            } else {
                result.addError("Not Found status");
            }
        });

        if (result.isError() || result.isWarning()) {
            return Mono.just(ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(result));
        }

        var res = transactionTemplate.execute(state -> {
            var save = entityManager.merge(dataInsert);
            state.flush();
            return save;
        });

        if (res.isClOnline()) {
            return reportRunnerManager
                .run(res)
                .flatMap(val -> {
                    if(val.getMessage() != null && val.getMessage().containsKey(EMessage.ERROR)) {
                        return Mono.just(ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(val));
                    }
                    return getFile(res.getCkId());
                });
        }

        result.setCkId(res.getCkId());
        return Mono.just(ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(result));
    }

    public Mono<ResponseEntity<Object>> getFile(ServerWebExchange exchange, UUID id, String user){
        var result = Result
                .builder()
                .build();
        var queue = queryFactory.selectFrom(QTQueue.tQueue)
                .where(QTQueue.tQueue.ckId.eq(id))
                .fetchOne();

        if (queue == null) {
            return Mono.just(
                ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(result.addError("Not Found queue"))
            );
        }
        return authorizationManager
                .getAuthorization(queue.getReport().getAuthorization())
                .authorization(exchange, user)
                .flatMap(val -> getFile(id));
    }

    public Mono<ResponseEntity<Object>> getFile(UUID id) {
        var result = Result
                .builder()
                .build();
        var queue = queryFactory
            .selectFrom(QTQueue.tQueue)
            .where(QTQueue.tQueue.ckId.eq(id))
            .fetchOne();
        if (queue == null) {
            result.addError("Not Found queue");
        }

        if (result.isError() || result.isWarning()) {
            return Mono.just(ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(result));
        }

        var file = storageManager.getStorage().getFile(queue);

        if (file == null) {
            return Mono.just(ResponseEntity
                    .ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(result.addError("Not Found file"))
            );
        }

        return Mono.just(
            ResponseEntity
                .ok()
                .header("Content-Type", file.getContentType())
                .header("Content-Disposition", String.format("attachment; filename=%s", file.getName()))
                .body(file.getFile())
        );
    }
}
