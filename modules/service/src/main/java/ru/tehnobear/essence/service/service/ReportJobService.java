package ru.tehnobear.essence.service.service;

import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.DefaultLockingTaskExecutor;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.core.LockingTaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Mono;
import ru.tehnobear.essence.dao.dto.EStatus;
import ru.tehnobear.essence.dao.entries.QTDStatus;
import ru.tehnobear.essence.dao.entries.QTQueue;
import ru.tehnobear.essence.dao.entries.QTScheduler;
import ru.tehnobear.essence.dao.entries.TQueue;
import ru.tehnobear.essence.share.manager.ReportRunnerManager;
import ru.tehnobear.essence.share.manager.StorageManager;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReportJobService {
    private final ConcurrentHashMap<UUID, Boolean> runMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, ScheduledFuture<?>> schedulerMap = new ConcurrentHashMap<>();
    private final JPAQueryFactory queryFactory;
    private final ReportRunnerManager reportRunnerManager;
    private final TransactionTemplate transactionTemplate;
    private final StorageManager storageManager;
    private final LockProvider lockProvider;
    private final TaskScheduler reportTaskScheduler;
    private final EntityManager entityManager;
    private final CronParser reportCronParser;
    private final String appReportName;

    @Async("appThreadPoolTaskExecutor")
    public void runReport() {
        Mono.when(queryFactory.selectFrom(QTQueue.tQueue)
            .where(
                QTQueue.tQueue.status.ckId.eq(EStatus.NEW)
                .and(QTQueue.tQueue.clOnline.eq(false))
                .and(QTQueue.tQueue.clDeleted.eq(false))
                .and(QTQueue.tQueue.report.clDeleted.eq(false))
                .and(QTQueue.tQueue.ckId.notIn(runMap.keySet()))
            )
            .orderBy(QTQueue.tQueue.cnPriority.asc())
            .fetch()
            .stream().map(queue -> {
                runMap.put(queue.getCkId(), true);
                return reportRunnerManager.run(queue)
                    .doFinally(sign -> {
                        runMap.remove(queue.getCkId());
                    });
            })
            .toList()
        )
        .onErrorResume(err -> {
            log.warn(err.getLocalizedMessage(), err);
            return Mono.empty();
        })
        .block();
    }

    @Async("appThreadPoolTaskExecutor")
    public void clearReport() {
        transactionTemplate.execute(status -> {
            var queues = queryFactory.selectFrom(QTQueue.tQueue)
                    .where(QTQueue.tQueue.status.ckId.eq(EStatus.SUCCESS).and(QTQueue.tQueue.ctCleaning.after(Instant.now())))
                    .fetch();
            storageManager.getStorage().deleteFile(queues.toArray(new TQueue[0]));
            queryFactory.update(QTQueue.tQueue)
                    .where(QTQueue.tQueue.in(queues))
                    .set(QTQueue.tQueue.status.ckId, EStatus.DELETE)
                    .execute();
            status.flush();
            return null;
        });
    }

    @Async("appThreadPoolTaskExecutor")
    public void reRunReport() {
        transactionTemplate.execute(status -> {
            var now = Instant.now().minus(5, ChronoUnit.MINUTES);
            queryFactory.update(QTQueue.tQueue)
                    .where(QTQueue
                            .tQueue.status.ckId.eq(EStatus.PROCESSING)
                            .and(QTQueue.tQueue.clOnline.eq(false))
                            .and(QTQueue.tQueue.clDeleted.eq(false))
                            .and(QTQueue.tQueue.server.ctChange.before(now))
                    )
                    .set(QTQueue.tQueue.status.ckId, EStatus.NEW)
                    .execute();
            status.flush();
            return null;
        });
    }

    @Async("appThreadPoolTaskExecutor")
    public void registrationNewScheduler() {
        Instant now = Instant.now();
        queryFactory.selectFrom(QTScheduler.tScheduler)
            .where(
                QTScheduler.tScheduler.ckId.notIn(schedulerMap.keySet())
                .and(QTScheduler.tScheduler.clEnable.eq(true))
                .and(QTScheduler.tScheduler.clDeleted.eq(false))
                .and(QTScheduler.tScheduler.ctStartRunCron.eq(now).or(QTScheduler.tScheduler.ctStartRunCron.before(now)))
            )
            .fetch()
            .forEach(sc -> {
                try {
                    log.debug("Registration scheduler {}", sc.getCkId());
                    var cron = reportCronParser.parse(sc.getCvUnixCron());
                    var nowReg = ZonedDateTime.now();
                    var reg = reportTaskScheduler.schedule(() -> {
                        log.debug("Run scheduler {}", sc.getCkId());
                        LockingTaskExecutor executor = new DefaultLockingTaskExecutor(lockProvider);
                        Instant createdAt = Instant.now();
                        Duration lockAtMostFor = Duration.ofSeconds(60);
                        Duration lockAtLeastFor = Duration.ZERO;
                        executor.executeWithLock((Runnable) () -> {
                            try {
                                transactionTemplate.execute(status -> {
                                    var execute = ZonedDateTime.now();
                                    var executionTime = ExecutionTime.forCron(cron);
                                    var nextExecution = executionTime.nextExecution(execute);
                                    nextExecution.ifPresent(zonedDateTime -> queryFactory.update(QTScheduler.tScheduler)
                                            .where(QTScheduler.tScheduler.ckId.eq(sc.getCkId()))
                                            .set(QTScheduler.tScheduler.ctNextRunCron, zonedDateTime.toInstant())
                                            .execute());
                                    var dataInsert = TQueue.builder()
                                            .ckId(UUID.randomUUID())
                                            .report(sc.getReport())
                                            .cctParameter(sc.getCctParameter())
                                            .format(sc.getFormat())
                                            .scheduler(sc)
                                            .queue(sc.getReport().getQueue())
                                            .clOnline(false)
                                            .ckUser(sc.getCkUser())
                                            .cvReportName(sc.getCvReportName())
                                            .cnPriority(sc.getCnPriority() == null ? sc.getReport().getCnPriority() : sc.getCnPriority())
                                            .status(queryFactory.selectFrom(QTDStatus.tDStatus)
                                                    .where(QTDStatus.tDStatus.ckId.eq(EStatus.NEW))
                                                    .fetchOne())
                                            .build();
                                    entityManager.merge(dataInsert);
                                    status.flush();
                                    return null;
                                });
                            } catch (Exception e){
                                log.error("Error run scheduler {}", sc.getCkId(), e);
                            }
                        }, new LockConfiguration(createdAt, "sc_report_" + sc.getCkId().toString(), lockAtMostFor, lockAtLeastFor));
                    }, new CronTrigger(cron.asString()));
                    transactionTemplate.execute(status -> {
                        var executionTime = ExecutionTime.forCron(cron);
                        var nextExecution = executionTime.nextExecution(nowReg);
                        nextExecution.ifPresent(zonedDateTime -> queryFactory.update(QTScheduler.tScheduler)
                                .where(QTScheduler.tScheduler.ckId.eq(sc.getCkId()))
                                .set(QTScheduler.tScheduler.ctNextRunCron, zonedDateTime.toInstant())
                                .execute());
                        status.flush();
                        return null;
                    });
                    schedulerMap.put(sc.getCkId(), reg);
                } catch (Exception e){
                    log.error("Error registration scheduler {}", sc.getCkId(), e);
                }
            });
        queryFactory.selectFrom(QTScheduler.tScheduler)
            .where(
                QTScheduler.tScheduler.ckId.in(schedulerMap.keySet())
                    .and(QTScheduler.tScheduler.clEnable.eq(false).or(QTScheduler.tScheduler.clDeleted.eq(true)))
            )
            .fetch()
            .forEach(sc -> {
                try {
                    var reg = schedulerMap.remove(sc.getCkId());
                    if (reg != null) {
                        reg.cancel(true);
                    }
                } catch (Exception e){
                    log.error("Error remove registration scheduler {}", sc.getCkId(), e);
                }
            });
    }
}
