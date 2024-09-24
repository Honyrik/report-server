package ru.tehnobear.essence.share.store;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import ru.tehnobear.essence.dao.entries.QTQueueStorage;
import ru.tehnobear.essence.dao.entries.TQueue;
import ru.tehnobear.essence.dao.entries.TQueueStorage;
import ru.tehnobear.essence.share.dto.FileStore;
import ru.tehnobear.essence.share.exception.ReportException;
import ru.tehnobear.essence.share.plugin.StoragePlugin;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class DBStorage implements StoragePlugin {
    private final JPAQueryFactory queryFactory;
    private final TransactionTemplate transactionTemplate;
    private final EntityManager entityManager;
    @Override
    public void saveFile(TQueue queue, FileStore file) {
        transactionTemplate.execute(status -> {
            var old = queryFactory.selectFrom(QTQueueStorage.tQueueStorage)
                        .where(QTQueueStorage.tQueueStorage.queue.eq(queue))
                        .fetchOne();
            if (old != null) {
                queryFactory
                    .update(QTQueueStorage.tQueueStorage)
                    .where(QTQueueStorage.tQueueStorage.ckId.eq(old.getCkId()))
                    .set(QTQueueStorage.tQueueStorage.cvName, file.getName())
                    .set(QTQueueStorage.tQueueStorage.ctChange, Instant.now())
                    .set(QTQueueStorage.tQueueStorage.cvContentType, file.getContentType())
                    .set(QTQueueStorage.tQueueStorage.cbResult, file.getFile())
                    .execute();
            } else {
                entityManager.merge(TQueueStorage.builder()
                        .ckId(queue.getCkId())
                        .queue(queue)
                        .ckUser(queue.getCkUser())
                        .ctChange(Instant.now())
                        .ctCreate(Instant.now())
                        .cbResult(file.getFile())
                        .cvContentType(file.getContentType())
                        .cvName(file.getName())
                        .build());
            }

            return null;
        });
    }

    @Override
    public FileStore getFile(TQueue queue) {
        var file = queryFactory
                .selectFrom(QTQueueStorage.tQueueStorage)
                .where(QTQueueStorage.tQueueStorage.queue.eq(queue))
                .fetchOne();
        if (file == null) {
            throw new ReportException("Not found file");
        }
        return FileStore.builder()
                .file(file.getCbResult())
                .contentType(file.getCvContentType())
                .name(file.getCvName())
                .build();
    }

    @Override
    public void deleteFile(TQueue... queue) {
        transactionTemplate.execute(status -> {
            queryFactory
                    .delete(QTQueueStorage.tQueueStorage)
                    .where(QTQueueStorage.tQueueStorage.queue.in(queue))
                    .execute();

            return null;
        });
    }
}
