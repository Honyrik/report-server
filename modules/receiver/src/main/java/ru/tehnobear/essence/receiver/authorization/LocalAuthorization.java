package ru.tehnobear.essence.receiver.authorization;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.tehnobear.essence.dao.entries.TAuthorization;
import ru.tehnobear.essence.share.exception.ForbiddenException;
import ru.tehnobear.essence.share.plugin.AuthorizationPlugin;

import java.util.Map;

@Slf4j
public class LocalAuthorization implements AuthorizationPlugin {
    protected final String name;
    protected final Map<String, Object> param;
    public LocalAuthorization(TAuthorization authorization, Map<String, Object> param) {
        this.name = authorization.getCvName();
        this.param = param;
    }
    @Override
    public Mono<String> authorization(ServerWebExchange exchange, String user) {
        return ReactiveSecurityContextHolder
                .getContext()
                .map(val ->
                        val.getAuthentication() != null ? val.getAuthentication().getName() : null)
                .defaultIfEmpty("")
                .map(val -> {
                    if (val == null || val.isEmpty()) {
                        throw new ForbiddenException("Need auth");
                    }
                    return val;
                });
    }
}
