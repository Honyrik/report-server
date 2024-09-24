package ru.tehnobear.essence.share.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import ru.tehnobear.essence.dao.entries.TLog;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuditService {
    private final EntityManager entityManager;
    private final TransactionTemplate transactionTemplate;

    @Transactional
    public void save(TLog tLog) {
        try {
            transactionTemplate.execute(state -> {
                entityManager.merge(tLog);
                state.flush();

                return null;
            });
        } catch (Exception e) {
            log.error(e.getLocalizedMessage(), e);
        }
    }
    public void saveError(TLog tLog, Throwable err) {
        tLog.setCvError(ExceptionUtils.getStackTrace(err));
        save(tLog);
    }

}
