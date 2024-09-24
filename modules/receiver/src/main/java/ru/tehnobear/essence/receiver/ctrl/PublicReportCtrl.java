package ru.tehnobear.essence.receiver.ctrl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.tehnobear.essence.dao.entries.TLog;
import ru.tehnobear.essence.receiver.api.PublicReportApi;
import ru.tehnobear.essence.receiver.dto.admin.PublicReportBody;
import ru.tehnobear.essence.receiver.service.PublicReportService;
import ru.tehnobear.essence.receiver.service.admin.UserService;
import ru.tehnobear.essence.share.service.AuditService;
import ru.tehnobear.essence.share.util.QueryUtil;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class PublicReportCtrl implements PublicReportApi {
    private final PublicReportService service;
    private final UserService userService;
    private final AuditService auditService;

    @Override
    public Mono<ResponseEntity<Object>> run(ServerWebExchange exchange, PublicReportBody data) {
        return userService
            .getUser()
            .flatMap(user -> {
                var tLog = TLog.builder()
                    .ccJson(QueryUtil.util.writeValueAsStringSilentAll(data))
                    .cvId(Optional.ofNullable(data.getReportId().toString())
                            .orElse(""))
                    .ckUser(user)
                    .cvAction("POST")
                    .cvTable("Run");
                return Mono.defer(() -> service.run(exchange, data, user))
                    .doOnSuccess(val -> auditService.save(tLog.build()))
                    .doOnError(err -> auditService.saveError(
                            tLog.build(),
                            err
                    ));
            });
    }

    @Override
    public Mono<ResponseEntity<Object>> getFile(ServerWebExchange exchange, UUID id) {
        return userService
                .getUser()
                .flatMap(user -> Mono.defer(() -> service.getFile(exchange, id, user)));
    }
}
