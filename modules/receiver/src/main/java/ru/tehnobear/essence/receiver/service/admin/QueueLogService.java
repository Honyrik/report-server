package ru.tehnobear.essence.receiver.service.admin;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.tehnobear.essence.dao.entries.QTQueueLog;
import ru.tehnobear.essence.receiver.dto.admin.QueueLogFetch;
import ru.tehnobear.essence.receiver.dto.admin.QueueLogResult;
import ru.tehnobear.essence.share.util.QueryUtil;

@Component
@Slf4j
@RequiredArgsConstructor
public class QueueLogService {
    private final JPAQueryFactory queryFactory;
    public Mono<QueueLogResult> fetch(QueueLogFetch fetch) {
        var query = queryFactory.selectFrom(QTQueueLog.tQueueLog);
        if (!fetch.getData().containsKey("clDeleted")) {
            fetch.getData().put("clDeleted", false);
        }
        QueryUtil.util.filterAndSort(query, fetch, QTQueueLog.tQueueLog);
        var res = query.fetchResults();

        return Mono.just(
            QueueLogResult
                .builder()
                .total(res.getTotal())
                .data(res.getResults())
                .build()
        );
    }
}
